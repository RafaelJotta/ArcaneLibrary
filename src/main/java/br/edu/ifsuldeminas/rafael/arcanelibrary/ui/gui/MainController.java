package br.edu.ifsuldeminas.rafael.arcanelibrary.ui.gui;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationObserver;
import br.edu.ifsuldeminas.rafael.arcanelibrary.simulation.SimulationConfig;
import br.edu.ifsuldeminas.rafael.arcanelibrary.simulation.SimulationEngine;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SynchronizationSnapshot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController implements SimulationObserver {

    @FXML private ListView<String> filaListView;
    @FXML private Pane palcoAnimacaoPane;
    @FXML private VBox metricasVBox;
    @FXML private ComboBox<String> cenarioComboBox;
    @FXML private Button btnIniciar;
    @FXML private Button btnParar;
    @FXML private TextArea logTextArea;

    private SimulationEngine currentEngine;
    private EventBus eventBus;
    private SimulationConfig config;

    // Organiza os magos POR LIVRO que eles estão acessando
    private final Map<String, Map<String, ProcessInfo>> mesasDeLivros = new LinkedHashMap<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private record ProcessInfo(AccessRequest process, MageState state) {}

    // Assets Visuais
    private Image imgLivro;
    private Image imgMagoComum;
    private Image imgMagoCritico;
    private Image imgMagoEscritor;

    @FXML
    public void initialize() {
        config = SimulationConfig.defaultConfig();

        cenarioComboBox.getItems().addAll(
                "1. Sincronização Padrão (1 Livro)",
                "2. Granularidade (3 Livros)",
                "3. Modo Caos (Sem Sincronização)",
                "4. Abraço Mortal (Deadlock)",
                "5. Inanição (Starvation)"
        );
        cenarioComboBox.getSelectionModel().selectFirst();

        btnParar.setDisable(true);
        btnIniciar.setOnAction(e -> iniciarSimulacao());
        btnParar.setOnAction(e -> pararSimulacao());

        carregarImagens();
    }

    private void carregarImagens() {
        try {
            imgLivro = new Image(getClass().getResourceAsStream("/br/edu/ifsuldeminas/rafael/arcanelibrary/ui/gui/assets/livro.png"));
            imgMagoComum = new Image(getClass().getResourceAsStream("/br/edu/ifsuldeminas/rafael/arcanelibrary/ui/gui/assets/mago_azul.png"));
            imgMagoCritico = new Image(getClass().getResourceAsStream("/br/edu/ifsuldeminas/rafael/arcanelibrary/ui/gui/assets/mago_roxo.png"));
            imgMagoEscritor = new Image(getClass().getResourceAsStream("/br/edu/ifsuldeminas/rafael/arcanelibrary/ui/gui/assets/mago_vermelho.png"));
        } catch (Exception e) {
            System.out.println("⚠️ Imagens não encontradas na pasta assets. Usando formas geométricas de fallback.");
        }
    }

    private void iniciarSimulacao() {
        logTextArea.clear();
        filaListView.getItems().clear();
        metricasVBox.getChildren().clear();
        mesasDeLivros.clear();
        palcoAnimacaoPane.getChildren().clear();

        eventBus = new EventBus();
        eventBus.addObserver(this);

        int selectedIndex = cenarioComboBox.getSelectionModel().getSelectedIndex();
        switch (selectedIndex) {
            case 0 -> currentEngine = SimulationEngine.defaultScenario(config, eventBus);
            case 1 -> currentEngine = SimulationEngine.libraryScenario(config, eventBus);
            case 2 -> currentEngine = SimulationEngine.chaosScenario(config, eventBus);
            case 3 -> currentEngine = SimulationEngine.deadlockScenario(config, eventBus);
            case 4 -> currentEngine = SimulationEngine.starvationScenario(config, eventBus);
        }

        btnIniciar.setDisable(true);
        cenarioComboBox.setDisable(true);
        btnParar.setDisable(false);

        new Thread(() -> currentEngine.start(), "Thread-Engine-Principal").start();
    }

    private void pararSimulacao() {
        if (currentEngine != null) {
            currentEngine.stop();
            String relatorio = currentEngine.metricsReport();
            Platform.runLater(() -> logTextArea.appendText("\n" + relatorio + "\n"));
        }
        btnIniciar.setDisable(false);
        cenarioComboBox.setDisable(false);
        btnParar.setDisable(true);
    }

    @Override
    public void onEvent(SimulationEvent event) {
        Platform.runLater(() -> {
            try {
                String time = TIME_FORMATTER.format(event.timestamp().atZone(ZoneId.systemDefault()));
                String processName = event.process() == null ? "[SISTEMA]" : event.process().label();
                logTextArea.appendText(String.format("[%s] %-25s %s\n", time, processName, event.message()));

                if (event.process() != null && event.state() != null) {
                    atualizarFilaVisual(event.process(), event.state());
                    String msgSegura = event.message() != null ? event.message() : "";
                    atualizarEstadoPalco(event.process(), event.state(), msgSegura);
                }

                if (event.snapshot() != null) {
                    atualizarMetricasDireita(event.snapshot());
                }
            } catch (Exception e) {
                System.err.println("Erro ao renderizar frame: " + e.getMessage());
            }
        });
    }

    private void atualizarFilaVisual(AccessRequest process, MageState state) {
        filaListView.getItems().removeIf(item -> item.contains(process.label()));

        if (state == MageState.RESTING || state == MageState.STOPPED) return;

        String sigla = switch (process.role()) {
            case SIMPLE_CONSULTATION -> "[CS]";
            case CRITICAL_RESEARCH -> "[PC]";
            case MAGICAL_RITUAL -> "[RM]";
            case CRITICAL_RITUAL -> "[RC]";
        };

        String status = state == MageState.WAITING ? "⏳ Aguardando" :
                (state == MageState.WRITING ? "✍️ Escrevendo" : "📖 Lendo");

        filaListView.getItems().add(String.format("%s %s %s", status, sigla, process.label()));
    }

    private void atualizarEstadoPalco(AccessRequest process, MageState state, String message) {
        if (state == MageState.READING || state == MageState.WRITING) {
            String nomeLivro = "Grimório Desconhecido";
            if (message.contains("'")) {
                int start = message.indexOf("'") + 1;
                int end = message.indexOf("'", start);
                if (start > 0 && end > start) {
                    nomeLivro = message.substring(start, end);
                }
            }
            mesasDeLivros.putIfAbsent(nomeLivro, new LinkedHashMap<>());
            mesasDeLivros.get(nomeLivro).put(process.label(), new ProcessInfo(process, state));
        } else {
            for (Map<String, ProcessInfo> magosNoLivro : mesasDeLivros.values()) {
                magosNoLivro.remove(process.label());
            }
        }
        desenharMutiplosPalcos();
    }

    private void desenharMutiplosPalcos() {
        palcoAnimacaoPane.getChildren().clear();

        int qtdLivros = mesasDeLivros.size();
        if (qtdLivros == 0) return;

        double width = palcoAnimacaoPane.getWidth();
        double height = palcoAnimacaoPane.getHeight();
        if (width == 0) width = 700;
        if (height == 0) height = 600;

        // LÓGICA DE TAMANHO DINÂMICO
        int tamanhoLivroImg = (qtdLivros == 1) ? 170 : 100; // Se for 1, livro grande. Se 3, médio.
        int tamanhoFonteLivro = (qtdLivros == 1) ? 16 : 13;
        double radiusMago = (qtdLivros == 1) ? 160 : 120; // Afasta mais se o livro for grande
        int tamanhoMagoImg = (qtdLivros == 1) ? 65 : 55; // Magos maiores no cenário de 1 livro

        int i = 0;
        for (Map.Entry<String, Map<String, ProcessInfo>> mesa : mesasDeLivros.entrySet()) {
            String nomeLivro = mesa.getKey();
            Map<String, ProcessInfo> magos = mesa.getValue();

            // Se for só 1 livro, centraliza um pouco mais para cima para dar presença
            double cx = width / 2.0;
            double cy = (height / 2.0) + (qtdLivros == 1 ? 40 : 80);

            if (qtdLivros > 1) {
                cx = (width / 2.0) + 210 * Math.cos(2 * Math.PI * i / qtdLivros - Math.PI / 2);
                cy = (height / 2.0) + 80 + 210 * Math.sin(2 * Math.PI * i / qtdLivros - Math.PI / 2);
            }

            // --- LIVRO ---
            int tamanhoCaixaLivro = tamanhoLivroImg + 40; // Espaço extra pro texto embaixo
            StackPane nodeLivro = criarLivroGrafico(nomeLivro, tamanhoLivroImg, tamanhoFonteLivro);
            nodeLivro.setPrefSize(tamanhoCaixaLivro, tamanhoCaixaLivro);
            nodeLivro.setLayoutX(cx - (tamanhoCaixaLivro / 2.0));
            nodeLivro.setLayoutY(cy - (tamanhoCaixaLivro / 2.0));
            palcoAnimacaoPane.getChildren().add(nodeLivro);

            // --- MAGOS ---
            int idxMago = 0;
            int totalMagos = magos.size();
            for (ProcessInfo info : magos.values()) {
                double angle = 2 * Math.PI * idxMago / (totalMagos > 0 ? totalMagos : 1);

                double mx = cx + radiusMago * Math.cos(angle);
                double my = cy + radiusMago * Math.sin(angle);

                int tamanhoCaixaMago = tamanhoMagoImg + 30; // Espaço extra pro nome
                StackPane magoVisual = criarMagoGrafico(info, tamanhoMagoImg);
                magoVisual.setPrefSize(tamanhoCaixaMago, tamanhoCaixaMago);
                magoVisual.setLayoutX(mx - (tamanhoCaixaMago / 2.0));
                magoVisual.setLayoutY(my - (tamanhoCaixaMago / 2.0));
                palcoAnimacaoPane.getChildren().add(magoVisual);

                idxMago++;
            }
            i++;
        }
    }

    private StackPane criarLivroGrafico(String titulo, int sizeImg, int sizeFont) {
        StackPane node = new StackPane();
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        if (imgLivro != null) {
            ImageView view = new ImageView(imgLivro);
            view.setFitWidth(sizeImg);
            view.setFitHeight(sizeImg);
            view.setPreserveRatio(true);
            box.getChildren().add(view);
        } else {
            Circle c = new Circle(sizeImg / 2.0, Color.web("#d4af37"));
            box.getChildren().add(c);
        }

        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-text-fill: #d4af37; -fx-font-weight: bold; -fx-font-size: " + sizeFont + "px; -fx-alignment: center;");
        box.getChildren().add(lbl);

        node.getChildren().add(box);
        return node;
    }

    private StackPane criarMagoGrafico(ProcessInfo info, int sizeImg) {
        StackPane node = new StackPane();
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Image imgSprite = switch (info.process().role()) {
            case SIMPLE_CONSULTATION -> imgMagoComum;
            case CRITICAL_RESEARCH -> imgMagoCritico;
            case MAGICAL_RITUAL -> imgMagoEscritor;
            case CRITICAL_RITUAL -> imgMagoEscritor;
        };

        if (imgSprite != null) {
            ImageView view = new ImageView(imgSprite);
            view.setFitWidth(sizeImg);
            view.setFitHeight(sizeImg);
            view.setPreserveRatio(true);

            DropShadow glow = new DropShadow();
            glow.setColor(info.state() == MageState.WRITING ? Color.RED : Color.CYAN);
            glow.setRadius(10);
            view.setEffect(glow);
            box.getChildren().add(view);
        } else {
            Circle aura = new Circle(sizeImg / 3.0);
            aura.setFill(switch (info.process().role()) {
                case SIMPLE_CONSULTATION -> Color.web("#1e90ff");
                case CRITICAL_RESEARCH -> Color.web("#9b59b6");
                case MAGICAL_RITUAL -> Color.web("#e74c3c");
                case CRITICAL_RITUAL -> Color.web("#f1c40f");
            });
            box.getChildren().add(aura);
        }

        Label nome = new Label(info.process().shortName());
        nome.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");

        box.getChildren().addAll(nome);
        node.getChildren().add(box);
        return node;
    }

    private void atualizarMetricasDireita(SynchronizationSnapshot snap) {
        metricasVBox.getChildren().clear();

        VBox cardAcesso = criarCartaoBase("ACESSO ATUAL");
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        if (snap.writerActive()) {
            lblStatus.setText("⛔ ESCRITOR ATIVO");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #e74c3c;");
        } else if (snap.activeReaders() > 0) {
            lblStatus.setText("📖 " + snap.activeReaders() + " LEITOR(ES)");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #1e90ff;");
        } else {
            lblStatus.setText("✅ LIVRE");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #2ecc71;");
        }
        cardAcesso.getChildren().add(lblStatus);

        VBox cardFila = criarCartaoBase("FILAS DE ESPERA");
        cardFila.getChildren().addAll(
                criarTextoInfo("Escritores: ", snap.waitingWriters(), "#e74c3c"),
                criarTextoInfo("Leitores VIP: ", snap.waitingCriticalReaders(), "#9b59b6"),
                criarTextoInfo("Leitores Comuns: ", snap.waitingCommonReaders(), "#1e90ff")
        );

        VBox cardCotas = criarCartaoBase("MÉTRICAS DO MOTOR");
        cardCotas.getChildren().addAll(
                criarTextoInfo("Lote Leitores: ", snap.commonReaderBatchQuota(), "#ffffff"),
                criarTextoInfo("Burst VIP Atual: ", snap.criticalVipBurst() + "/" + snap.maxCriticalVipBurst(), "#f1c40f"),
                criarTextoInfo("Leituras Concluídas: ", (int) snap.completedReads(), "#7bed9f"),
                criarTextoInfo("Escritas Concluídas: ", (int) snap.completedWrites(), "#ff7f50")
        );

        metricasVBox.getChildren().addAll(cardAcesso, cardFila, cardCotas);
    }

    private VBox criarCartaoBase(String titulo) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-background-color: #2f3542; -fx-padding: 10; -fx-background-radius: 5;");
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #a4b0be; -fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().add(lblTitulo);
        return box;
    }

    private Label criarTextoInfo(String label, int valor, String corHex) {
        Label l = new Label(label + valor);
        l.setStyle("-fx-text-fill: " + corHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }
    private Label criarTextoInfo(String label, String valor, String corHex) {
        Label l = new Label(label + valor);
        l.setStyle("-fx-text-fill: " + corHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }
}