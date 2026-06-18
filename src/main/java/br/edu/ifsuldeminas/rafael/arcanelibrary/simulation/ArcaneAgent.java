package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.AccessPermit;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
import br.edu.ifsuldeminas.rafael.arcanelibrary.utils.RandomDuration;

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
    public void run() { // O modificador 'final' foi removido daqui
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int index = ThreadLocalRandom.current().nextInt(grimoires.size());
                this.currentGrimoire = grimoires.get(index);
                this.currentCoordinator = coordinators.get(index);

                rest();

                publish(EventType.WAITING, ProcessState.WAITING, "Solicitou acesso a: " + currentGrimoire.title());

                try (AccessPermit permit = currentCoordinator.acquire(descriptor)) {
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