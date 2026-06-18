package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageAccessType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.Grimoire;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SyncCoordinator;
import java.util.List;

public final class AgentFactory {
    private AgentFactory() {}

    public static ArcaneAgent create(AccessRequest d, List<Grimoire> g, List<SyncCoordinator> c, SimulationConfig config, EventBus eb) {
        return switch (d.role()) {
            case SIMPLE_CONSULTATION -> new CommonReaderAgent(d, g, c, config, eb);
            case CRITICAL_RESEARCH -> new CriticalReaderAgent(d, g, c, config, eb);
            case MAGICAL_RITUAL, CRITICAL_RITUAL -> new WriterAgent(d, g, c, config, eb);
        };
    }
}