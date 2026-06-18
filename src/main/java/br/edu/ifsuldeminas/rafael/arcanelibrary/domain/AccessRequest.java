package br.edu.ifsuldeminas.rafael.arcanelibrary.domain;

import java.util.Objects;

public record AccessRequest(int id, String name, MageAccessType type) {

    public AccessRequest {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }

        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }

    public String shortName() {
        return "%s #%d".formatted(name, id);
    }

    public String label() {
        return "[%s] %s".formatted(type.token(), shortName());
    }

    public MageAccessType role() {
        return type;
    }
}
