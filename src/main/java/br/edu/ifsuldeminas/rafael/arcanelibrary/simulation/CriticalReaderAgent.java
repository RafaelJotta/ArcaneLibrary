package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
import br.edu.ifsuldeminas.rafael.arcanelibrary.utils.RandomDuration;
import java.time.Duration;
import java.util.List;

public final class CriticalReaderAgent extends ArcaneAgent {
    public CriticalReaderAgent(ProcessDescriptor d, List<Grimoire> g, List<SyncCoordinator> c, SimulationConfig conf, EventBus eb) {
        super(d, g, c, conf, eb);
    }
    @Override protected Duration activityDuration() { return RandomDuration.between(config().minCriticalRead(), config().maxCriticalRead()); }
    @Override protected void enterCriticalRegion() { publish(EventType.STATE, ProcessState.READING, grimoire().read(descriptor())); }
}