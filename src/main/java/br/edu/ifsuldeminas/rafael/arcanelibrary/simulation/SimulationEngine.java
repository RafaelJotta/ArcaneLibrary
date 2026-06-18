package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageAccessType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class SimulationEngine {

    private final List<Grimoire> grimoires;
    private final List<SyncCoordinator> coordinators;
    private final EventBus eventBus;
    private final List<ArcaneAgent> agents;
    private final List<Thread> threads;

    private SimulationEngine(
            List<Grimoire> grimoires,
            List<SyncCoordinator> coordinators,
            EventBus eventBus,
            List<ArcaneAgent> agents
    ) {
        this.grimoires = grimoires;
        this.coordinators = coordinators;
        this.eventBus = eventBus;
        this.agents = agents;
        this.threads = new ArrayList<>();
    }

    public static SimulationEngine defaultScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new ArcaneSynchronizationCoordinator(
                config.maxCriticalReadersBeforeWriter(),
                eventBus
        );

        List<ArcaneAgent> agents = buildDescriptors(config).stream()
                .map(descriptor -> AgentFactory.create(
                        descriptor,
                        List.of(grimoire),
                        List.of(coordinator),
                        config,
                        eventBus
                ))
                .toList();

        return new SimulationEngine(
                List.of(grimoire),
                List.of(coordinator),
                eventBus,
                agents
        );
    }

    public static SimulationEngine libraryScenario(SimulationConfig config, EventBus eventBus) {
        List<Grimoire> libraryBooks = List.of(
                new Grimoire("Codex Umbrae"),
                new Grimoire("Grimorio de Fogo"),
                new Grimoire("Tomo Proibido")
        );

        List<SyncCoordinator> libraryCoordinators = libraryBooks.stream()
                .map(book -> (SyncCoordinator) new ArcaneSynchronizationCoordinator(
                        config.maxCriticalReadersBeforeWriter(),
                        eventBus
                ))
                .toList();

        List<ArcaneAgent> agents = buildDescriptors(config).stream()
                .map(descriptor -> AgentFactory.create(
                        descriptor,
                        libraryBooks,
                        libraryCoordinators,
                        config,
                        eventBus
                ))
                .toList();

        return new SimulationEngine(
                libraryBooks,
                libraryCoordinators,
                eventBus,
                agents
        );
    }

    public static SimulationEngine chaosScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new ChaosCoordinator();

        List<ArcaneAgent> agents = buildDescriptors(config).stream()
                .map(descriptor -> AgentFactory.create(
                        descriptor,
                        List.of(grimoire),
                        List.of(coordinator),
                        config,
                        eventBus
                ))
                .toList();

        return new SimulationEngine(
                List.of(grimoire),
                List.of(coordinator),
                eventBus,
                agents
        );
    }

    public static SimulationEngine deadlockScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire fireBook = new Grimoire("Tomo do Fogo");
        Grimoire iceBook = new Grimoire("Pergaminho do Gelo");

        SyncCoordinator fireCoordinator = new ArcaneSynchronizationCoordinator(
                config.maxCriticalReadersBeforeWriter(),
                eventBus
        );

        SyncCoordinator iceCoordinator = new ArcaneSynchronizationCoordinator(
                config.maxCriticalReadersBeforeWriter(),
                eventBus
        );

        AccessRequest desc1 = new AccessRequest(
                1,
                "Ritualista do Fogo",
                MageAccessType.MAGICAL_RITUAL
        );

        DeadlockAgent agent1 = new DeadlockAgent(
                desc1,
                fireBook,
                iceBook,
                fireCoordinator,
                iceCoordinator,
                eventBus
        );

        AccessRequest desc2 = new AccessRequest(
                2,
                "Ritualista do Gelo",
                MageAccessType.MAGICAL_RITUAL
        );

        DeadlockAgent agent2 = new DeadlockAgent(
                desc2,
                iceBook,
                fireBook,
                iceCoordinator,
                fireCoordinator,
                eventBus
        );

        List<ArcaneAgent> agents = List.of(
                new ArcaneAgent(desc1, List.of(fireBook), List.of(fireCoordinator), config, eventBus) {
                    @Override
                    public void run() {
                        agent1.run();
                    }

                    @Override
                    public ProcessMetrics metrics() {
                        return agent1.metrics();
                    }

                    @Override
                    protected java.time.Duration activityDuration() {
                        return java.time.Duration.ZERO;
                    }

                    @Override
                    protected void enterCriticalRegion() {
                        // Agente especial de deadlock delega a execução ao DeadlockAgent.
                    }
                },
                new ArcaneAgent(desc2, List.of(iceBook), List.of(iceCoordinator), config, eventBus) {
                    @Override
                    public void run() {
                        agent2.run();
                    }

                    @Override
                    public ProcessMetrics metrics() {
                        return agent2.metrics();
                    }

                    @Override
                    protected java.time.Duration activityDuration() {
                        return java.time.Duration.ZERO;
                    }

                    @Override
                    protected void enterCriticalRegion() {
                        // Agente especial de deadlock delega a execução ao DeadlockAgent.
                    }
                }
        );

        return new SimulationEngine(
                List.of(fireBook, iceBook),
                List.of(fireCoordinator, iceCoordinator),
                eventBus,
                agents
        );
    }

    public static SimulationEngine starvationScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new StarvationCoordinator();

        SimulationConfig starvationConfig = new SimulationConfig(
                config.duration(),

                Math.max(config.simpleReaders(), 20),
                config.criticalReaders(),
                Math.max(config.writers(), 1),
                config.criticalWriters(),

                config.maxCriticalReadersBeforeWriter(),

                java.time.Duration.ofMillis(10),
                java.time.Duration.ofMillis(50),

                config.minRead(),
                config.maxRead(),

                config.minCriticalRead(),
                config.maxCriticalRead(),

                config.minWrite(),
                config.maxWrite(),

                config.minCriticalWrite(),
                config.maxCriticalWrite()
        );

        List<ArcaneAgent> agents = buildDescriptors(starvationConfig).stream()
                .map(descriptor -> AgentFactory.create(
                        descriptor,
                        List.of(grimoire),
                        List.of(coordinator),
                        starvationConfig,
                        eventBus
                ))
                .toList();

        return new SimulationEngine(
                List.of(grimoire),
                List.of(coordinator),
                eventBus,
                agents
        );
    }

    private static List<AccessRequest> buildDescriptors(SimulationConfig config) {
        List<AccessRequest> descriptors = new ArrayList<>();
        int nextId = 1;

        nextId = addGroup(
                descriptors,
                nextId,
                config.simpleReaders(),
                "Consultor Arcano",
                MageAccessType.SIMPLE_CONSULTATION
        );

        nextId = addGroup(
                descriptors,
                nextId,
                config.criticalReaders(),
                "Pesquisador Critico",
                MageAccessType.CRITICAL_RESEARCH
        );

        nextId = addGroup(
                descriptors,
                nextId,
                config.writers(),
                "Ritualista Arcano",
                MageAccessType.MAGICAL_RITUAL
        );

        addGroup(
                descriptors,
                nextId,
                config.criticalWriters(),
                "Ritualista Critico",
                MageAccessType.CRITICAL_RITUAL
        );

        return descriptors;
    }

    private static int addGroup(
            List<AccessRequest> descriptors,
            int startId,
            int amount,
            String namePrefix,
            MageAccessType type
    ) {
        int nextId = startId;

        for (int i = 1; i <= amount; i++) {
            descriptors.add(new AccessRequest(
                    nextId,
                    namePrefix + " " + i,
                    type
            ));

            nextId++;
        }

        return nextId;
    }

    public void start() {
        publishSystem("Iniciando simulacao arcana...");

        for (ArcaneAgent agent : agents) {
            Thread thread = Thread.ofVirtual()
                    .name(agent.metrics().process().label())
                    .start(agent);

            threads.add(thread);
        }
    }

    public void stop() {
        publishSystem("Encerrando simulacao e interrompendo processos.");

        for (Thread thread : threads) {
            thread.interrupt();
        }

        for (Thread thread : threads) {
            joinQuietly(thread);
        }
    }

    public String metricsReport() {
        StringBuilder report = new StringBuilder();

        report.append("\nRELATORIO FINAL DE METRICAS (DESEMPENHO E CONCORRENCIA)\n");
        report.append("=".repeat(85)).append('\n');
        report.append("%-28s %-20s %8s %12s %12s %12s%n"
                .formatted("Processo", "Tipo", "Acessos", "EsperaTotal", "EsperaMedia", "EsperaMax"));

        agents.stream()
                .map(ArcaneAgent::metrics)
                .sorted(Comparator.comparingInt(metric -> metric.process().id()))
                .forEach(metric -> {
                    report.append("%-28s %-20s %8d %12d %12d %12d%n".formatted(
                            metric.process().shortName(),
                            metric.process().role().displayName(),
                            metric.accesses(),
                            metric.totalWaitMillis(),
                            metric.averageWaitMillis(),
                            metric.maxWaitMillis()
                    ));

                    if (!metric.accessesByBook().isEmpty()) {
                        String distribution = metric.accessesByBook()
                                .entrySet()
                                .stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .collect(Collectors.joining(", "));

                        report.append("   -> Distribuicao: [")
                                .append(distribution)
                                .append("]\n");
                    }
                });

        report.append("=".repeat(85)).append('\n');
        report.append("ESTADO FINAL DOS MONITORES (RECURSOS):\n");

        for (int i = 0; i < coordinators.size(); i++) {
            SynchronizationSnapshot snapshot = coordinators.get(i).snapshot();
            String resourceName = grimoires.size() > i
                    ? grimoires.get(i).title()
                    : "Monitor " + (i + 1);

            report.append(String.format("%-20s: ", resourceName))
                    .append(snapshot.compact())
                    .append('\n');
        }

        return report.toString();
    }

    private void publishSystem(String message) {
        SynchronizationSnapshot snapshot = coordinators.isEmpty()
                ? null
                : coordinators.get(0).snapshot();

        eventBus.publish(new SimulationEvent(
                Instant.now(),
                EventType.SYSTEM,
                null,
                null,
                message,
                snapshot
        ));
    }

    private void joinQuietly(Thread thread) {
        try {
            thread.join(1_000L);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}