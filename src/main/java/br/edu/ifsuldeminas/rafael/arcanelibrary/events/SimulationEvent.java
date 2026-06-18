package br.edu.ifsuldeminas.rafael.arcanelibrary.events;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SynchronizationSnapshot;

import java.time.Instant;

public record SimulationEvent(
        Instant timestamp,
        EventType type,
        AccessRequest process,
        MageState state,
        String message,
        SynchronizationSnapshot snapshot
) {
    public static SimulationEvent now(
            EventType type,
            AccessRequest process,
            MageState state,
            String message,
            SynchronizationSnapshot snapshot
    ) {
        return new SimulationEvent(Instant.now(), type, process, state, message, snapshot);
    }
}
