package com.arcane.scriptorium.synchronization;

import com.arcane.scriptorium.domain.ProcessDescriptor;

public final class StarvationCoordinator implements SyncCoordinator {
    private int activeReaders = 0;
    private boolean writerActive = false;

    @Override
    public synchronized AccessPermit acquire(ProcessDescriptor process) throws InterruptedException {
        long start = System.currentTimeMillis();

        if (process.role().isReader()) {
            // Leitor só espera se tiver um Escritor escrevendo AGORA.
            // Ele atropela qualquer escritor que esteja na fila de espera.
            while (writerActive) {
                wait();
            }
            activeReaders++;
        } else {
            // Escritor espera se tiver QUALQUER pessoa (leitor ou escritor).
            // Ele nunca tem prioridade.
            while (writerActive || activeReaders > 0) {
                wait();
            }
            writerActive = true;
        }

        long waited = System.currentTimeMillis() - start;
        return new AccessPermit(this, process, waited);
    }

    @Override
    public synchronized void release(ProcessDescriptor process) {
        if (process.role().isReader()) {
            activeReaders--;
        } else {
            writerActive = false;
        }
        // Acorda todo mundo na fila. Como os leitores não ligam para a fila
        // de escritores, eles sempre roubam a vaga novamente.
        notifyAll();
    }

    @Override
    public SynchronizationSnapshot snapshot() {
        return new SynchronizationSnapshot(activeReaders, writerActive, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}