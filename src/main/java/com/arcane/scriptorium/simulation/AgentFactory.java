package com.arcane.scriptorium.simulation;

import com.arcane.scriptorium.domain.AccessRole;
import com.arcane.scriptorium.domain.Grimoire;
import com.arcane.scriptorium.domain.ProcessDescriptor;
import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.synchronization.SyncCoordinator;
import java.util.List;

public final class AgentFactory {
    private AgentFactory() {}

    public static ArcaneAgent create(ProcessDescriptor d, List<Grimoire> g, List<SyncCoordinator> c, SimulationConfig config, EventBus eb) {
        return switch (d.role()) {
            case COMMON_READER -> new CommonReaderAgent(d, g, c, config, eb);
            case CRITICAL_READER -> new CriticalReaderAgent(d, g, c, config, eb);
            case WRITER -> new WriterAgent(d, g, c, config, eb);
        };
    }
}