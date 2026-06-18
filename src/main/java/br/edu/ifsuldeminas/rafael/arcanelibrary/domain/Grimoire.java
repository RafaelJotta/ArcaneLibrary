package br.edu.ifsuldeminas.rafael.arcanelibrary.domain;

public final class Grimoire {

    private final String title;
    private int revision;
    private String lastInscription;

    public Grimoire(String title) {
        this.title = title;
        this.revision = 1;
        this.lastInscription = "Indice dos encantamentos estavel.";
    }

    public String title() {
        return title;
    }

    public synchronized int revision() {
        return revision;
    }

    public synchronized String lastInscription() {
        return lastInscription;
    }

    public synchronized String read(AccessRequest request) {
        return "%s consultou '%s' rev.%d: %s"
                .formatted(request.shortName(), title, revision, lastInscription);
    }

    public synchronized String criticalRead(AccessRequest request) {
        return "%s realizou pesquisa critica em '%s' rev.%d: %s"
                .formatted(request.shortName(), title, revision, lastInscription);
    }

    public synchronized String write(AccessRequest request) {
        revision += 1;
        lastInscription = "Runa revisada por " + request.shortName();

        return "%s atualizou '%s' para rev.%d"
                .formatted(request.shortName(), title, revision);
    }

    public synchronized String criticalWrite(AccessRequest request) {
        revision += 1;
        lastInscription = "Ritual critico registrado por " + request.shortName();

        return "%s executou ritual critico em '%s' para rev.%d"
                .formatted(request.shortName(), title, revision);
    }
}
