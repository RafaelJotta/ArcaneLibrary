package com.arcane.scriptorium.simulation;

import com.arcane.scriptorium.domain.Grimoire;
import com.arcane.scriptorium.domain.ProcessDescriptor;
import com.arcane.scriptorium.domain.ProcessState;
import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.events.EventType;
import com.arcane.scriptorium.synchronization.SyncCoordinator;
import com.arcane.scriptorium.utils.RandomDuration;
import java.time.Duration;
import java.util.List;

public final class CommonReaderAgent extends ArcaneAgent {
    public CommonReaderAgent(ProcessDescriptor d, List<Grimoire> g, List<SyncCoordinator> c, SimulationConfig conf, EventBus eb) {
        super(d, g, c, conf, eb);
    }
    @Override protected Duration activityDuration() { return RandomDuration.between(config().minRead(), config().maxRead()); }
    @Override protected void enterCriticalRegion() { publish(EventType.STATE, ProcessState.READING, grimoire().read(descriptor())); }
}