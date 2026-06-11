package com.arcane.scriptorium;

import com.arcane.scriptorium.events.EventBus;
import com.arcane.scriptorium.simulation.SimulationConfig;
import com.arcane.scriptorium.simulation.SimulationEngine;
import com.arcane.scriptorium.ui.console.Ansi;
import com.arcane.scriptorium.ui.console.ConsoleEventRenderer;

import java.util.Scanner;

public final class MainTerminal {

    private MainTerminal() {
    }

    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        eventBus.addObserver(new ConsoleEventRenderer(Ansi.isEnabled()));

        SimulationConfig config = SimulationConfig.defaultConfig()
                .withMaxCriticalReadersBeforeWriter(parseCriticalLimit(args));

        Scanner scanner = new Scanner(System.in);
        boolean rodando = true;

        while (rodando) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🧙 BIBLIOTECA ARCANA - MENU DE CONTROLE 🧙");
            System.out.println("=".repeat(60));
            System.out.println("[1] Simulacao Padrao (1 Grimorio, Sincronizacao Perfeita)");
            System.out.println("[2] Biblioteca Arcana (Multiplos Grimorios) - Em construcao");
            System.out.println("[3] Modo Caos (Simular Race Condition) - Em construcao");
            System.out.println("[4] Abraco Mortal (Simular Deadlock)");
            System.out.println("[5] Inanicao (Simular Starvation dos Escritores)"); // <- NOVO
            System.out.println("[0] Sair");
            System.out.print("Escolha o cenario para apresentar: ");

            String opcao = scanner.nextLine();
            SimulationEngine engine = null;

            switch (opcao) {
                case "1":
                    engine = SimulationEngine.defaultScenario(config, eventBus);
                    break;
                case "2":
                    engine = SimulationEngine.libraryScenario(config, eventBus);
                    break;
                case "3":
                    engine = SimulationEngine.chaosScenario(config, eventBus);
                    break;
                case "4":
                    engine = SimulationEngine.deadlockScenario(config, eventBus);
                    break;
                case "5":
                    engine = SimulationEngine.starvationScenario(config, eventBus);
                    break;
                case "0":
                    System.out.println("Fechando as portas da Biblioteca...");
                    rodando = false;
                    continue;
                default:
                    System.out.println(Ansi.paint(Ansi.isEnabled(), Ansi.RED, "Opcao invalida. Tente novamente."));
                    continue;
            }

            if (engine != null) {
                engine.start();
                System.out.println("\n" + Ansi.paint(Ansi.isEnabled(), Ansi.GREEN,
                        ">>> Simulacao rodando em background... Pressione [ENTER] a qualquer momento para interromper e gerar as metricas <<<"));

                // O terminal fica aguardando o professor/você apertar ENTER
                scanner.nextLine();

                engine.stop();

                System.out.println("\n" + engine.metricsReport());
                System.out.println("\n" + Ansi.paint(Ansi.isEnabled(), Ansi.YELLOW,
                        "DICA: Esta execucao ocorreu com a prevencao de inanicao (starvation) ATIVADA."));
            }
        }
        scanner.close();
    }

    private static int parseCriticalLimit(String[] args) {
        if (args.length < 2) {
            return SimulationConfig.defaultConfig().maxCriticalReadersBeforeWriter();
        }
        try {
            return Math.max(0, Integer.parseInt(args[1]));
        } catch (NumberFormatException ignored) {
            return SimulationConfig.defaultConfig().maxCriticalReadersBeforeWriter();
        }
    }
}