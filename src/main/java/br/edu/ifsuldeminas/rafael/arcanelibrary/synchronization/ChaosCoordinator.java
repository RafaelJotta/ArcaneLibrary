package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;

public final class ChaosCoordinator implements SyncCoordinator {
    @Override
    public AccessPermit acquire(ProcessDescriptor process) {
        return new AccessPermit(this, process, 0);
    }

    @Override
    public void release(ProcessDescriptor process) {}

    @Override
    public SynchronizationSnapshot snapshot() {
        return new SynchronizationSnapshot(0, false, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}