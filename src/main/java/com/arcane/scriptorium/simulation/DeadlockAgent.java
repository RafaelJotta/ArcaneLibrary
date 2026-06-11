package com.arcane.scriptorium.simulation;

import com.arcane.scriptorium.domain.Grimoire;
import com.arcane.scriptorium.domain.ProcessDescriptor;
import com.arcane.scriptorium.domain.ProcessState;
import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.events.EventType;
import com.arcane.scriptorium.events.SimulationEvent;
import com.arcane.scriptorium.synchronization.AccessPermit;
import com.arcane.scriptorium.synchronization.SyncCoordinator;

public final class DeadlockAgent implements Runnable {
    private final ProcessDescriptor descriptor;
    private final Grimoire primaryGrimoire;
    private final Grimoire secondaryGrimoire;
    private final SyncCoordinator primaryCoordinator;
    private final SyncCoordinator secondaryCoordinator;
    private final EventBus eventBus;
    private final ProcessMetrics metrics;

    public DeadlockAgent(ProcessDescriptor descriptor,
                         Grimoire primaryGrimoire, Grimoire secondaryGrimoire,
                         SyncCoordinator primaryCoordinator, SyncCoordinator secondaryCoordinator,
                         EventBus eventBus) {
        this.descriptor = descriptor;
        this.primaryGrimoire = primaryGrimoire;
        this.secondaryGrimoire = secondaryGrimoire;
        this.primaryCoordinator = primaryCoordinator;
        this.secondaryCoordinator = secondaryCoordinator;
        this.eventBus = eventBus;
        this.metrics = new ProcessMetrics(descriptor);
    }

    @Override
    public void run() {
        try {
            publish(ProcessState.WAITING, "Tentando abrir o primeiro livro: " + primaryGrimoire.title());

            try (AccessPermit permit1 = primaryCoordinator.acquire(descriptor)) {
                metrics.registerAccess(permit1.waitedMillis(), primaryGrimoire.title());
                publish(ProcessState.READING, "Segurando " + primaryGrimoire.title() + ". Aguardando para pegar o segundo...");

                Thread.sleep(100);

                publish(ProcessState.WAITING, "Sem soltar o primeiro, tenta abrir o segundo: " + secondaryGrimoire.title());

                try (AccessPermit permit2 = secondaryCoordinator.acquire(descriptor)) {
                    metrics.registerAccess(permit2.waitedMillis(), secondaryGrimoire.title());
                    publish(ProcessState.WRITING, "Sucesso improvavel! Tem os dois livros.");
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            publish(ProcessState.STOPPED, "Processo interrompido (provavelmente pelo fim da simulacao).");
        }
    }

    public ProcessMetrics metrics() {
        return metrics.snapshot();
    }

    private void publish(ProcessState state, String message) {
        eventBus.publish(SimulationEvent.now(EventType.STATE, descriptor, state, message, primaryCoordinator.snapshot()));
    }
}