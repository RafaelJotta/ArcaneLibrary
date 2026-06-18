package br.edu.ifsuldeminas.rafael.arcanelibrary.events;

@FunctionalInterface
public interface SimulationObserver {
    void onEvent(SimulationEvent event);
}
