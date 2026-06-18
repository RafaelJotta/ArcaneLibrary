package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.AccessPermit;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;

public final class DeadlockAgent implements Runnable {
    private final AccessRequest descriptor;
    private final Grimoire primaryGrimoire;
    private final Grimoire secondaryGrimoire;
    private final SyncCoordinator primaryCoordinator;
    private final SyncCoordinator secondaryCoordinator;
    private final EventBus eventBus;
    private final ProcessMetrics metrics;

    public DeadlockAgent(AccessRequest descriptor,
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
            publish(MageState.WAITING, "Tentando abrir o primeiro livro: " + primaryGrimoire.title());

            try (AccessPermit permit1 = primaryCoordinator.acquire(descriptor)) {
                metrics.registerAccess(permit1.waitedMillis(), primaryGrimoire.title());
                publish(MageState.READING, "Segurando " + primaryGrimoire.title() + ". Aguardando para pegar o segundo...");

                Thread.sleep(100);

                publish(MageState.WAITING, "Sem soltar o primeiro, tenta abrir o segundo: " + secondaryGrimoire.title());

                try (AccessPermit permit2 = secondaryCoordinator.acquire(descriptor)) {
                    metrics.registerAccess(permit2.waitedMillis(), secondaryGrimoire.title());
                    publish(MageState.WRITING, "Sucesso improvavel! Tem os dois livros.");
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            publish(MageState.STOPPED, "Processo interrompido (provavelmente pelo fim da simulacao).");
        }
    }

    public ProcessMetrics metrics() {
        return metrics.snapshot();
    }

    private void publish(MageState state, String message) {
        eventBus.publish(SimulationEvent.now(EventType.STATE, descriptor, state, message, primaryCoordinator.snapshot()));
    }
}