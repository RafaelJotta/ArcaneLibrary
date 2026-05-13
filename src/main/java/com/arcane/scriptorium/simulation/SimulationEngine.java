package com.arcane.scriptorium.simulation;

import com.arcane.scriptorium.domain.AccessRole;
import com.arcane.scriptorium.domain.Grimoire;
import com.arcane.scriptorium.domain.ProcessDescriptor;
import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.events.EventType;
import com.arcane.scriptorium.events.SimulationEvent;
import com.arcane.scriptorium.synchronization.*;

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

    private SimulationEngine(List<Grimoire> g, List<SyncCoordinator> c, EventBus eb, List<ArcaneAgent> a) {
        this.grimoires = g;
        this.coordinators = c;
        this.eventBus = eb;
        this.agents = a;
        this.threads = new ArrayList<>();
    }

    public static SimulationEngine defaultScenario(SimulationConfig config, EventBus eb) {
        Grimoire g = new Grimoire("Codex Umbrae");
        SyncCoordinator c = new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eb);
        List<ArcaneAgent> a = getStandardDescriptors().stream()
                .map(d -> AgentFactory.create(d, List.of(g), List.of(c), config, eb)).toList();
        return new SimulationEngine(List.of(g), List.of(c), eb, a);
    }

    public static SimulationEngine libraryScenario(SimulationConfig config, EventBus eb) {
        List<Grimoire> g = List.of(new Grimoire("Codex Umbrae"), new Grimoire("Grimorio de Fogo"), new Grimoire("Tomo Proibido"));
        List<SyncCoordinator> c = g.stream()
                .map(b -> (SyncCoordinator) new ArcaneSynchronizationCoordinator(config.maxCriticalReadersBeforeWriter(), eb)).toList();
        List<ArcaneAgent> a = getStandardDescriptors().stream()
                .map(d -> AgentFactory.create(d, g, c, config, eb)).toList();
        return new SimulationEngine(g, c, eb, a);
    }

    public static SimulationEngine chaosScenario(SimulationConfig config, EventBus eb) {
        Grimoire g = new Grimoire("Codex Umbrae");
        SyncCoordinator c = new ChaosCoordinator();
        List<ArcaneAgent> a = getStandardDescriptors().stream()
                .map(d -> AgentFactory.create(d, List.of(g), List.of(c), config, eb)).toList();
        return new SimulationEngine(List.of(g), List.of(c), eb, a);
    }

    private static List<ProcessDescriptor> getStandardDescriptors() {
        return List.of(
                new ProcessDescriptor(1, "Mago Harry", AccessRole.COMMON_READER),
                new ProcessDescriptor(2, "Maga Hermione", AccessRole.COMMON_READER),
                new ProcessDescriptor(3, "Mago Ron", AccessRole.COMMON_READER),
                new ProcessDescriptor(4, "Feiticeiro Voldemort", AccessRole.CRITICAL_READER),
                new ProcessDescriptor(5, "Feiticeiro Sauron", AccessRole.CRITICAL_READER),
                new ProcessDescriptor(6, "Anciao Gandalf", AccessRole.WRITER),
                new ProcessDescriptor(7, "Anciao Dumbledore", AccessRole.WRITER)
        );
    }

    public void start() {
        publishSystem("Iniciando simulacao da Biblioteca Arcana.");
        for (ArcaneAgent a : agents) {
            Thread t = new Thread(a, a.metrics().process().label());
            t.start();
            threads.add(t);
        }
    }

    public void stop() {
        publishSystem("Encerrando simulacao e interrompendo agentes.");
        for (Thread t : threads) t.interrupt();
        for (Thread t : threads) { try { t.join(1000); } catch (Exception e) {} }
    }

    public String metricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("\nRelatorio final de metricas\n");
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

                    // ATUALIZADO: Adiciona a linha extra com a distribuição de livros
                    if (!metric.accessesByBook().isEmpty()) {
                        String books = metric.accessesByBook().entrySet().stream()
                                .map(e -> e.getKey() + ": " + e.getValue())
                                .collect(Collectors.joining(", "));
                        report.append("   -> Acessos: [").append(books).append("]\n");
                    }
                });

        report.append("=".repeat(85)).append('\n');
        report.append("Estado final dos Monitores (Livros):\n");
        for (int i = 0; i < coordinators.size(); i++) {
            SynchronizationSnapshot snapshot = coordinators.get(i).snapshot();
            String bookName = grimoires.size() > i ? grimoires.get(i).title() : "Monitor " + (i + 1);
            report.append(String.format("%-20s: ", bookName)).append(snapshot.compact()).append('\n');
        }
        return report.toString();
    }

    private void publishSystem(String message) {
        SynchronizationSnapshot snapshot = coordinators.isEmpty() ? null : coordinators.get(0).snapshot();
        eventBus.publish(new SimulationEvent(Instant.now(), EventType.SYSTEM, null, null, message, snapshot));
    }
}