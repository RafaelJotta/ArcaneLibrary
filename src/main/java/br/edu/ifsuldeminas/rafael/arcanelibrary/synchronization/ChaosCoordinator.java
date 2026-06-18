package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;

public final class ChaosCoordinator implements SyncCoordinator {
    @Override
    public AccessPermit acquire(AccessRequest process) {
        return new AccessPermit(this, process, 0);
    }

    @Override
    public void release(AccessRequest process) {}

    @Override
    public SynchronizationSnapshot snapshot() {
        return new SynchronizationSnapshot(0, false, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}