package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRole;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
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