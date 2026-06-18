package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.ProcessDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class ProcessMetrics {
    private final ProcessDescriptor process;
    private final AtomicInteger accesses;
    private final AtomicLong totalWaitMillis;
    private final AtomicLong maxWaitMillis;
    // NOVO: Rastreador de acessos por livro
    private final ConcurrentHashMap<String, AtomicInteger> accessesByBook;

    public ProcessMetrics(ProcessDescriptor process) {
        this.process = process;
        this.accesses = new AtomicInteger();
        this.totalWaitMillis = new AtomicLong();
        this.maxWaitMillis = new AtomicLong();
        this.accessesByBook = new ConcurrentHashMap<>();
    }

    private ProcessMetrics(ProcessDescriptor process, int accesses, long totalWaitMillis, long maxWaitMillis, Map<String, Integer> accessesByBook) {
        this.process = process;
        this.accesses = new AtomicInteger(accesses);
        this.totalWaitMillis = new AtomicLong(totalWaitMillis);
        this.maxWaitMillis = new AtomicLong(maxWaitMillis);
        this.accessesByBook = new ConcurrentHashMap<>();
        accessesByBook.forEach((k, v) -> this.accessesByBook.put(k, new AtomicInteger(v)));
    }

    // ATUALIZADO: Agora ele recebe também o nome do livro acessado
    public void registerAccess(long waitMillis, String bookTitle) {
        accesses.incrementAndGet();
        totalWaitMillis.addAndGet(waitMillis);
        maxWaitMillis.accumulateAndGet(waitMillis, Math::max);
        accessesByBook.computeIfAbsent(bookTitle, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public ProcessMetrics snapshot() {
        Map<String, Integer> currentBooks = accessesByBook.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        return new ProcessMetrics(process, accesses.get(), totalWaitMillis.get(), maxWaitMillis.get(), currentBooks);
    }

    public ProcessDescriptor process() { return process; }
    public int accesses() { return accesses.get(); }
    public long totalWaitMillis() { return totalWaitMillis.get(); }
    public long maxWaitMillis() { return maxWaitMillis.get(); }

    public long averageWaitMillis() {
        int currentAccesses = accesses.get();
        return currentAccesses == 0 ? 0 : totalWaitMillis.get() / currentAccesses;
    }

    // NOVO: Devolve o mapa de livros para a Engine montar o relatório
    public Map<String, Integer> accessesByBook() {
        return accessesByBook.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
}