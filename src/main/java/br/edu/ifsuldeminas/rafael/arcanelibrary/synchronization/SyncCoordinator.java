package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;

public interface SyncCoordinator {
    AccessPermit acquire(AccessRequest process) throws InterruptedException;
    void release(AccessRequest process);
    SynchronizationSnapshot snapshot();
}