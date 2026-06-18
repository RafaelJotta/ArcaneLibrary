package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;

public interface SyncCoordinator {
    AccessPermit acquire(ProcessDescriptor process) throws InterruptedException;
    void release(ProcessDescriptor process);
    SynchronizationSnapshot snapshot();
}