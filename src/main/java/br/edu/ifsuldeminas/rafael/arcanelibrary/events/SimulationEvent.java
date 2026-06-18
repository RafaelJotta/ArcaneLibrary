package br.edu.ifsuldeminas.rafael.arcanelibrary.events;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SynchronizationSnapshot;

import java.time.Instant;

public record SimulationEvent(
        Instant timestamp,
        EventType type,
        ProcessDescriptor process,
        ProcessState state,
        String message,
        SynchronizationSnapshot snapshot
) {
    public static SimulationEvent now(
            EventType type,
            ProcessDescriptor process,
            ProcessState state,
            String message,
            SynchronizationSnapshot snapshot
    ) {
        return new SimulationEvent(Instant.now(), type, process, state, message, snapshot);
    }
}
