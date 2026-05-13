package com.arcane.scriptorium.synchronization;

import com.arcane.scriptorium.domain.ProcessDescriptor;

public interface SyncCoordinator {
    AccessPermit acquire(ProcessDescriptor process) throws InterruptedException;
    void release(ProcessDescriptor process);
    SynchronizationSnapshot snapshot();
}