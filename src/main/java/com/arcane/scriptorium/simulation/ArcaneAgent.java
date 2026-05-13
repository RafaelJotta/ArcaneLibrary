package com.arcane.scriptorium.simulation;

import com.arcane.scriptorium.domain.Grimoire;
import com.arcane.scriptorium.domain.ProcessDescriptor;
import com.arcane.scriptorium.domain.ProcessState;
import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.events.EventType;
import com.arcane.scriptorium.events.SimulationEvent;
import com.arcane.scriptorium.synchronization.AccessPermit;
import com.arcane.scriptorium.synchronization.SyncCoordinator;
import com.arcane.scriptorium.utils.RandomDuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class ArcaneAgent implements Runnable {
    private final ProcessDescriptor descriptor;
    private final List<Grimoire> grimoires;
    private final List<SyncCoordinator> coordinators;
    private final SimulationConfig config;
    private final EventBus eventBus;
    private final ProcessMetrics metrics;

    private Grimoire currentGrimoire;
    private SyncCoordinator currentCoordinator;

    protected ArcaneAgent(ProcessDescriptor descriptor, List<Grimoire> grimoires,
                          List<SyncCoordinator> coordinators, SimulationConfig config, EventBus eventBus) {
        this.descriptor = descriptor;
        this.grimoires = grimoires;
        this.coordinators = coordinators;
        this.config = config;
        this.eventBus = eventBus;
        this.metrics = new ProcessMetrics(descriptor);
    }

    @Override
    public final void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Sorteia primeiro!
                int index = ThreadLocalRandom.current().nextInt(grimoires.size());
                this.currentGrimoire = grimoires.get(index);
                this.currentCoordinator = coordinators.get(index);

                // Descansa depois.
                rest();

                // ATUALIZADO: Log seguro usando o currentGrimoire.title()
                publish(EventType.WAITING, ProcessState.WAITING, "Solicitou acesso a: " + currentGrimoire.title());

                try (AccessPermit permit = currentCoordinator.acquire(descriptor)) {
                    // ATUALIZADO: Passamos o nome do livro para a métrica
                    metrics.registerAccess(permit.waitedMillis(), currentGrimoire.title());
                    enterCriticalRegion();
                    Thread.sleep(activityDuration().toMillis());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ProcessMetrics metrics() { return metrics.snapshot(); }
    protected final Grimoire grimoire() { return currentGrimoire; }
    protected final ProcessDescriptor descriptor() { return descriptor; }
    protected final SimulationConfig config() { return config; }

    protected final void publish(EventType type, ProcessState state, String message) {
        eventBus.publish(SimulationEvent.now(type, descriptor, state, message, currentCoordinator.snapshot()));
    }

    protected abstract Duration activityDuration();
    protected abstract void enterCriticalRegion();

    private void rest() throws InterruptedException {
        publish(EventType.STATE, ProcessState.RESTING, "Descansando.");
        Thread.sleep(RandomDuration.between(config.minRest(), config.maxRest()).toMillis());
    }
}