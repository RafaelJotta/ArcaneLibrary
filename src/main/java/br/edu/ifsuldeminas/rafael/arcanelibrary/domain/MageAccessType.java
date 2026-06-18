package br.edu.ifsuldeminas.rafael.arcanelibrary.domain;

public enum MageAccessType {

    SIMPLE_CONSULTATION("Consulta simples", "CS", false, true, 1),
    CRITICAL_RESEARCH("Pesquisa critica", "PC", false, true, 3),
    MAGICAL_RITUAL("Ritual magico", "RM", true, false, 2),
    CRITICAL_RITUAL("Ritual critico", "RC", true, false, 4);

    private final String displayName;
    private final String token;
    private final boolean exclusive;
    private final boolean shared;
    private final int priority;

    MageAccessType(
            String displayName,
            String token,
            boolean exclusive,
            boolean shared,
            int priority
    ) {
        this.displayName = displayName;
        this.token = token;
        this.exclusive = exclusive;
        this.shared = shared;
        this.priority = priority;
    }

    public String displayName() {
        return displayName;
    }

    public String token() {
        return token;
    }

    public boolean requiresExclusiveAccess() {
        return exclusive;
    }

    public boolean allowsSharedAccess() {
        return shared;
    }

    public int priority() {
        return priority;
    }

    public boolean isReader() {
        return allowsSharedAccess();
    }

    public boolean isWriter() {
        return requiresExclusiveAccess();
    }
}
