package br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;

import java.time.Duration;

public record SimulationConfig(
        Duration duration,

        int simpleReaders,
        int criticalReaders,
        int writers,
        int criticalWriters,

        int maxCriticalReadersBeforeWriter,

        Duration minRest,
        Duration maxRest,

        Duration minRead,
        Duration maxRead,

        Duration minCriticalRead,
        Duration maxCriticalRead,

        Duration minWrite,
        Duration maxWrite,

        Duration minCriticalWrite,
        Duration maxCriticalWrite
) {
    public static SimulationConfig defaultConfig() {
        return new SimulationConfig(
                Duration.ofSeconds(15),

                3,  // leitores comuns
                2,  // pesquisadores críticos
                2,  // escritores
                1,  // escritores críticos

                5,

                Duration.ofMillis(250),
                Duration.ofMillis(850),

                Duration.ofMillis(500),
                Duration.ofMillis(1_000),

                Duration.ofMillis(350),
                Duration.ofMillis(750),

                Duration.ofMillis(700),
                Duration.ofMillis(1_150),

                Duration.ofMillis(900),
                Duration.ofMillis(1_400)
        );
    }

    public SimulationConfig withDuration(Duration newDuration) {
        return new SimulationConfig(
                newDuration,
                simpleReaders,
                criticalReaders,
                writers,
                criticalWriters,
                maxCriticalReadersBeforeWriter,
                minRest,
                maxRest,
                minRead,
                maxRead,
                minCriticalRead,
                maxCriticalRead,
                minWrite,
                maxWrite,
                minCriticalWrite,
                maxCriticalWrite
        );
    }

    public SimulationConfig withMaxCriticalReadersBeforeWriter(int newLimit) {
        return new SimulationConfig(
                duration,
                simpleReaders,
                criticalReaders,
                writers,
                criticalWriters,
                Math.max(0, newLimit),
                minRest,
                maxRest,
                minRead,
                maxRead,
                minCriticalRead,
                maxCriticalRead,
                minWrite,
                maxWrite,
                minCriticalWrite,
                maxCriticalWrite
        );
    }

    public SimulationConfig withAgentAmounts(
            int newSimpleReaders,
            int newCriticalReaders,
            int newWriters,
            int newCriticalWriters
    ) {
        return new SimulationConfig(
                duration,
                Math.max(0, newSimpleReaders),
                Math.max(0, newCriticalReaders),
                Math.max(0, newWriters),
                Math.max(0, newCriticalWriters),
                maxCriticalReadersBeforeWriter,
                minRest,
                maxRest,
                minRead,
                maxRead,
                minCriticalRead,
                maxCriticalRead,
                minWrite,
                maxWrite,
                minCriticalWrite,
                maxCriticalWrite
        );
    }
}