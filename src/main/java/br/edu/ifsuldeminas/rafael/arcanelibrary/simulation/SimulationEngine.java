package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRole;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;
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
            List<ArcaneAgent> agents) {
        this.grimoires = grimoires;
        this.coordinators = coordinators;
        this.eventBus = eventBus;
        this.agents = agents;
        this.threads = new ArrayList<>();
    }

    public static SimulationEngine defaultScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eventBus);

        List<ArcaneAgent> agents = getStandardDescriptors().stream()
                .map(descriptor -> AgentFactory.create(descriptor, List.of(grimoire), List.of(coordinator), config, eventBus))
                .toList();

        return new SimulationEngine(List.of(grimoire), List.of(coordinator), eventBus, agents);
    }

    public static SimulationEngine libraryScenario(SimulationConfig config, EventBus eventBus) {
        List<Grimoire> libraryBooks = List.of(new Grimoire("Codex Umbrae"), new Grimoire("Grimorio de Fogo"), new Grimoire("Tomo Proibido"));

        List<SyncCoordinator> libraryCoordinators = libraryBooks.stream()
                .map(b -> (SyncCoordinator) new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eventBus))
                .toList();

        List<ArcaneAgent> agents = getStandardDescriptors().stream()
                .map(d -> AgentFactory.create(d, libraryBooks, libraryCoordinators, config, eventBus))
                .toList();

        return new SimulationEngine(libraryBooks, libraryCoordinators, eventBus, agents);
    }

    public static SimulationEngine chaosScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new ChaosCoordinator();

        List<ArcaneAgent> agents = getStandardDescriptors().stream()
                .map(descriptor -> AgentFactory.create(descriptor, List.of(grimoire), List.of(coordinator), config, eventBus))
                .toList();

        return new SimulationEngine(List.of(grimoire), List.of(coordinator), eventBus, agents);
    }

    public static SimulationEngine deadlockScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire fireBook = new Grimoire("Tomo do Fogo");
        Grimoire iceBook = new Grimoire("Pergaminho do Gelo");

        SyncCoordinator fireCoordinator = new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eventBus);
        SyncCoordinator iceCoordinator = new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eventBus);

        ProcessDescriptor desc1 = new ProcessDescriptor(6, "Anciao Gandalf", AccessRole.WRITER);
        DeadlockAgent agent1 = new DeadlockAgent(desc1, fireBook, iceBook, fireCoordinator, iceCoordinator, eventBus);

        ProcessDescriptor desc2 = new ProcessDescriptor(7, "Anciao Dumbledore", AccessRole.WRITER);
        DeadlockAgent agent2 = new DeadlockAgent(desc2, iceBook, fireBook, iceCoordinator, fireCoordinator, eventBus);

        List<ArcaneAgent> agents = List.of(
                new ArcaneAgent(desc1, List.of(fireBook), List.of(fireCoordinator), config, eventBus) {
                    @Override public void run() { agent1.run(); }
                    @Override public ProcessMetrics metrics() { return agent1.metrics(); }
                    @Override protected java.time.Duration activityDuration() { return java.time.Duration.ZERO; }
                    @Override protected void enterCriticalRegion() {}
                },
                new ArcaneAgent(desc2, List.of(iceBook), List.of(iceCoordinator), config, eventBus) {
                    @Override public void run() { agent2.run(); }
                    @Override public ProcessMetrics metrics() { return agent2.metrics(); }
                    @Override protected java.time.Duration activityDuration() { return java.time.Duration.ZERO; }
                    @Override protected void enterCriticalRegion() {}
                }
        );

        return new SimulationEngine(List.of(fireBook, iceBook), List.of(fireCoordinator, iceCoordinator), eventBus, agents);
    }

    public static SimulationEngine starvationScenario(SimulationConfig config, EventBus eventBus) {
        Grimoire grimoire = new Grimoire("Codex Umbrae");
        SyncCoordinator coordinator = new StarvationCoordinator();

        SimulationConfig starvationConfig = new SimulationConfig(
                config.duration(),
                config.maxCriticalReadersBeforeWriter(),
                java.time.Duration.ofMillis(10),
                java.time.Duration.ofMillis(50),
                config.minRead(),
                config.maxRead(),
                config.minCriticalRead(),
                config.maxCriticalRead(),
                config.minWrite(),
                config.maxWrite()
        );

        List<ArcaneAgent> agents = getStandardDescriptors().stream()
                .map(descriptor -> AgentFactory.create(descriptor, List.of(grimoire), List.of(coordinator), starvationConfig, eventBus))
                .toList();

        return new SimulationEngine(List.of(grimoire), List.of(coordinator), eventBus, agents);
    }

    private static List<ProcessDescriptor> getStandardDescriptors() {
        return List.of(
                new ProcessDescriptor(1, "Mago Harry", AccessRole.COMMON_READER),
                new ProcessDescriptor(2, "Maga Hermione", AccessRole.COMMON_READER),
                new ProcessDescriptor(3, "Mago Ron", AccessRole.COMMON_READER),
                new ProcessDescriptor(4, "Feiticeiro Voldemort", AccessRole.CRITICAL_READER),
                new ProcessDescriptor(5, "Feiticeiro Sauron", AccessRole.CRITICAL_READER),
                new ProcessDescriptor(6, "Anciao Gandalf", AccessRole.WRITER),
                new ProcessDescriptor(7, "Anciao Dumbledore", AccessRole.WRITER));
    }

    public void start() {
        publishSystem("Iniciando simulacao arcana...");
        for (ArcaneAgent agent : agents) {
            Thread thread = new Thread(agent, agent.metrics().process().label());
            thread.start();
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
        report.append("%-28s %-16s %8s %12s %12s %12s%n"
                .formatted("Processo", "Tipo", "Acessos", "EsperaTotal", "EsperaMedia", "EsperaMax"));

        agents.stream()
                .map(ArcaneAgent::metrics)
                .sorted(Comparator.comparingInt(metric -> metric.process().id()))
                .forEach(metric -> {
                    report.append("%-28s %-16s %8d %12d %12d %12d%n".formatted(
                            metric.process().shortName(),
                            metric.process().role().displayName(),
                            metric.accesses(),
                            metric.totalWaitMillis(),
                            metric.averageWaitMillis(),
                            metric.maxWaitMillis()));

                    if (!metric.accessesByBook().isEmpty()) {
                        String distribution = metric.accessesByBook().entrySet().stream()
                                .map(e -> e.getKey() + ": " + e.getValue())
                                .collect(Collectors.joining(", "));
                        report.append("   -> Distribuicao: [").append(distribution).append("]\n");
                    }
                });

        report.append("=".repeat(85)).append('\n');
        report.append("ESTADO FINAL DOS MONITORES (RECURSOS):\n");
        for (int i = 0; i < coordinators.size(); i++) {
            SynchronizationSnapshot snapshot = coordinators.get(i).snapshot();
            String resourceName = grimoires.size() > i ? grimoires.get(i).title() : "Monitor " + (i + 1);
            report.append(String.format("%-20s: ", resourceName)).append(snapshot.compact()).append('\n');
        }
        return report.toString();
    }

    private void publishSystem(String message) {
        SynchronizationSnapshot snapshot = coordinators.isEmpty() ? null : coordinators.get(0).snapshot();
        eventBus.publish(new SimulationEvent(
                Instant.now(),
                EventType.SYSTEM,
                null,
                null,
                message,
                snapshot));
    }

    private void joinQuietly(Thread thread) {
        try {
            thread.join(1_000L);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}