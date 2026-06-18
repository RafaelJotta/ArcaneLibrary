package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageAccessType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
import br.edu.ifsuldeminas.rafael.arcanelibrary.utils.RandomDuration;

import java.time.Duration;
import java.util.List;

public final class WriterAgent extends ArcaneAgent {

    public WriterAgent(
            AccessRequest d,
            List<Grimoire> g,
            List<SyncCoordinator> c,
            SimulationConfig conf,
            EventBus eb
    ) {
        super(d, g, c, conf, eb);
    }

    @Override
    protected Duration activityDuration() {
        if (descriptor().role() == MageAccessType.CRITICAL_RITUAL) {
            return RandomDuration.between(config().minCriticalWrite(), config().maxCriticalWrite());
        }

        return RandomDuration.between(config().minWrite(), config().maxWrite());
    }

    @Override
    protected void enterCriticalRegion() {
        if (descriptor().role() == MageAccessType.CRITICAL_RITUAL) {
            publish(EventType.STATE, MageState.WRITING, grimoire().criticalWrite(descriptor()));
            return;
        }

        publish(EventType.STATE, MageState.WRITING, grimoire().write(descriptor()));
    }
}