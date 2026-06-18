package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
import br.edu.ifsuldeminas.rafael.arcanelibrary.utils.RandomDuration;
import java.time.Duration;
import java.util.List;

public final class CommonReaderAgent extends ArcaneAgent {
    public CommonReaderAgent(AccessRequest d, List<Grimoire> g, List<SyncCoordinator> c, SimulationConfig conf, EventBus eb) {
        super(d, g, c, conf, eb);
    }
    @Override protected Duration activityDuration() { return RandomDuration.between(config().minRead(), config().maxRead()); }
    @Override protected void enterCriticalRegion() { publish(EventType.STATE, MageState.READING, grimoire().read(descriptor())); }
}