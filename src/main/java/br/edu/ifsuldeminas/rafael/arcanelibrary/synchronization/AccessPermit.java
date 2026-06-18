package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;

public final class AccessPermit implements AutoCloseable {
    private final SyncCoordinator coordinator;
    private final ProcessDescriptor process;
    private final long waitedMillis;
    private boolean closed;

    AccessPermit(SyncCoordinator coordinator, ProcessDescriptor process, long waitedMillis) {
        this.coordinator = coordinator;
        this.process = process;
        this.waitedMillis = waitedMillis;
    }

    public long waitedMillis() { return waitedMillis; }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            coordinator.release(process);
        }
    }
}