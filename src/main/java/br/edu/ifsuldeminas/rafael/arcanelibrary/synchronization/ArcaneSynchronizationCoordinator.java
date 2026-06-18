package br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageAccessType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class ArcaneSynchronizationCoordinator implements SyncCoordinator {
    private final ReentrantLock policyMutex;
    private final Condition stateChanged;
    private final Semaphore criticalRegionGate;
    private final int maxCriticalVipBurst;
    private final EventBus eventBus;

    private int activeReaders;
    private boolean writerActive;
    private int waitingCommonReaders;
    private int waitingCriticalReaders;
    private int waitingWriters;
    private int commonReaderBatchQuota;
    private int criticalVipBurst;
    private long completedReads;
    private long completedWrites;

    public ArcaneSynchronizationCoordinator(int maxCriticalVipBurst, EventBus eventBus) {
        this.maxCriticalVipBurst = maxCriticalVipBurst;
        this.eventBus = eventBus;
        this.policyMutex = new ReentrantLock(true);
        this.stateChanged = policyMutex.newCondition();
        this.criticalRegionGate = new Semaphore(1, true);
    }

    @Override
    public AccessPermit acquire(AccessRequest process) throws InterruptedException {
        long startedAt = System.nanoTime();
        policyMutex.lockInterruptibly();
        boolean waitingRegistered = false;
        try {
            registerWaiting(process.role());
            waitingRegistered = true;
            publish(EventType.WAITING, process, MageState.WAITING, "Entrou na fila.");

            while (!canEnter(process.role())) {
                stateChanged.await();
            }

            unregisterWaiting(process.role());
            waitingRegistered = false;
            reserveCriticalRegion(process);

            long waitedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            return new AccessPermit(this, process, waitedMillis);
        } finally {
            policyMutex.unlock();
        }
    }

    @Override
    public void release(AccessRequest process) {
        policyMutex.lock();
        try {
            if (process.role().isReader()) {
                activeReaders -= 1;
                completedReads += 1;
                if (activeReaders == 0) criticalRegionGate.release();
            } else {
                writerActive = false;
                completedWrites += 1;
                criticalVipBurst = 0;
                commonReaderBatchQuota = waitingCommonReaders;
                criticalRegionGate.release();
            }
            stateChanged.signalAll();
        } finally {
            policyMutex.unlock();
        }
    }

    private boolean canEnter(MageAccessType role) {
        if (writerActive) return false;
        return switch (role) {
            case SIMPLE_CONSULTATION -> waitingWriters == 0 || commonReaderBatchQuota > 0;
            case CRITICAL_RESEARCH -> !(waitingWriters > 0 && criticalVipBurst >= maxCriticalVipBurst);
            case MAGICAL_RITUAL, CRITICAL_RITUAL -> activeReaders == 0 && commonReaderBatchQuota == 0 &&
                    (waitingCriticalReaders == 0 || (waitingWriters > 0 && criticalVipBurst >= maxCriticalVipBurst));
        };
    }

    private void reserveCriticalRegion(AccessRequest process) {
        MageAccessType role = process.role();
        if (role.isReader()) {
            if (activeReaders == 0) criticalRegionGate.acquireUninterruptibly();
            activeReaders += 1;
            if (role == MageAccessType.SIMPLE_CONSULTATION && commonReaderBatchQuota > 0) commonReaderBatchQuota -= 1;
            if (role == MageAccessType.CRITICAL_RESEARCH && waitingWriters > 0) criticalVipBurst += 1;
        } else {
            criticalRegionGate.acquireUninterruptibly();
            writerActive = true;
        }
    }

    private void registerWaiting(MageAccessType role) {
        switch (role) {
            case SIMPLE_CONSULTATION -> waitingCommonReaders += 1;
            case CRITICAL_RESEARCH -> waitingCriticalReaders += 1;
            case MAGICAL_RITUAL, CRITICAL_RITUAL -> waitingWriters += 1;
        }
    }

    private void unregisterWaiting(MageAccessType role) {
        switch (role) {
            case SIMPLE_CONSULTATION -> waitingCommonReaders -= 1;
            case CRITICAL_RESEARCH -> waitingCriticalReaders -= 1;
            case MAGICAL_RITUAL, CRITICAL_RITUAL -> waitingWriters -= 1;
        }
    }

    @Override
    public SynchronizationSnapshot snapshot() {
        return new SynchronizationSnapshot(activeReaders, writerActive, waitingCommonReaders,
                waitingCriticalReaders, waitingWriters, commonReaderBatchQuota, criticalVipBurst,
                maxCriticalVipBurst, completedReads, completedWrites);
    }

    private void publish(EventType type, AccessRequest process, MageState state, String message) {
        eventBus.publish(SimulationEvent.now(type, process, state, message, snapshot()));
    }
}