package br.edu.ifsuldeminas.rafael.arcanelibrary.ui.gui;

import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.AccessRequest;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageAccessType;
import br.edu.ifsuldeminas.rafael.arcanelibrary.domain.MageState;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.EventBus;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationEvent;
import br.edu.ifsuldeminas.rafael.arcanelibrary.events.SimulationObserver;
import br.edu.ifsuldeminas.rafael.arcanelibrary.simulation.SimulationConfig;
import br.edu.ifsuldeminas.rafael.arcanelibrary.simulation.SimulationEngine;
import br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization.SynchronizationSnapshot;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainController implements SimulationObserver {

    @FXML private StackPane appRoot;
    @FXML private StackPane introRoot;
    @FXML private StackPane intro3dContainer;
    @FXML private StackPane libraryViewport;
    @FXML private BorderPane dashboardRoot;

    @FXML private Button btnEntrar;
    @FXML private Button btnVoltarInicio;
    @FXML private Button btnIniciar;
    @FXML private Button btnParar;

    @FXML private ListView<String> filaListView;
    @FXML private VBox metricasVBox;
    @FXML private ComboBox<String> cenarioComboBox;
    @FXML private TextArea logTextArea;
    @FXML private Label lblSceneInfo;

    @FXML private TextField txtSimpleReaders;
    @FXML private TextField txtCriticalReaders;
    @FXML private TextField txtWriters;
    @FXML private TextField txtCriticalWriters;

    private SimulationEngine currentEngine;
    private EventBus eventBus;
    private SimulationConfig config;

    private Group activeBooksLayer;
    private Group activeAgentsLayer;

    private final Map<String, Map<String, ProcessInfo>> mesasDeLivros = new LinkedHashMap<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private record ProcessInfo(AccessRequest process, MageState state) {}

    @FXML
    public void initialize() {
        config = SimulationConfig.defaultConfig();

        cenarioComboBox.getItems().addAll(
                "1. Política Equilibrada (1 Grimório)",
                "2. Biblioteca com Múltiplos Grimórios",
                "3. Race Condition (Sem Sincronização)",
                "4. Deadlock (Reserva Cruzada)",
                "5. Starvation (Leitores Bloqueando Rituais)"
        );

        cenarioComboBox.getSelectionModel().selectFirst();

        txtSimpleReaders.setText(String.valueOf(config.simpleReaders()));
        txtCriticalReaders.setText(String.valueOf(config.criticalReaders()));
        txtWriters.setText(String.valueOf(config.writers()));
        txtCriticalWriters.setText(String.valueOf(config.criticalWriters()));

        btnParar.setDisable(true);

        btnEntrar.setOnAction(event -> entrarNaBiblioteca());
        btnVoltarInicio.setOnAction(event -> voltarParaPortal());
        btnIniciar.setOnAction(event -> iniciarSimulacao());
        btnParar.setOnAction(event -> pararSimulacao());

        aplicarEstiloBotaoIniciarNormal();

        montarPortal3D();
        montarBiblioteca3D();
        prepararEstadoInicial();
    }

    private void prepararEstadoInicial() {
        dashboardRoot.setOpacity(0.0);
        dashboardRoot.setScaleX(0.98);
        dashboardRoot.setScaleY(0.98);
        dashboardRoot.setDisable(true);

        introRoot.setOpacity(1.0);
        introRoot.setScaleX(1.0);
        introRoot.setScaleY(1.0);
        introRoot.setVisible(true);
        introRoot.setManaged(true);

        btnVoltarInicio.setVisible(false);
        btnVoltarInicio.setManaged(false);

        prepararMetricasIniciais();
        atualizarCenaBiblioteca3D();
    }

    private void entrarNaBiblioteca() {
        dashboardRoot.setDisable(false);

        btnVoltarInicio.setVisible(true);
        btnVoltarInicio.setManaged(true);

        FadeTransition fadeIntro = new FadeTransition(Duration.millis(750), introRoot);
        fadeIntro.setFromValue(1.0);
        fadeIntro.setToValue(0.0);

        ScaleTransition zoomIntro = new ScaleTransition(Duration.millis(750), introRoot);
        zoomIntro.setFromX(1.0);
        zoomIntro.setFromY(1.0);
        zoomIntro.setToX(1.12);
        zoomIntro.setToY(1.12);

        FadeTransition fadeDashboard = new FadeTransition(Duration.millis(750), dashboardRoot);
        fadeDashboard.setFromValue(0.0);
        fadeDashboard.setToValue(1.0);

        ScaleTransition zoomDashboard = new ScaleTransition(Duration.millis(750), dashboardRoot);
        zoomDashboard.setFromX(0.98);
        zoomDashboard.setFromY(0.98);
        zoomDashboard.setToX(1.0);
        zoomDashboard.setToY(1.0);

        TranslateTransition moveDashboard = new TranslateTransition(Duration.millis(750), dashboardRoot);
        moveDashboard.setFromY(18);
        moveDashboard.setToY(0);

        ParallelTransition transition = new ParallelTransition(
                fadeIntro,
                zoomIntro,
                fadeDashboard,
                zoomDashboard,
                moveDashboard
        );

        transition.setOnFinished(event -> {
            introRoot.setVisible(false);
            introRoot.setManaged(false);
        });

        transition.play();
    }

    private void voltarParaPortal() {
        if (currentEngine != null) {
            pararSimulacao();
        }

        introRoot.setVisible(true);
        introRoot.setManaged(true);
        introRoot.setOpacity(0.0);
        introRoot.setScaleX(1.08);
        introRoot.setScaleY(1.08);

        FadeTransition fadeDashboard = new FadeTransition(Duration.millis(600), dashboardRoot);
        fadeDashboard.setFromValue(1.0);
        fadeDashboard.setToValue(0.0);

        ScaleTransition zoomDashboard = new ScaleTransition(Duration.millis(600), dashboardRoot);
        zoomDashboard.setFromX(1.0);
        zoomDashboard.setFromY(1.0);
        zoomDashboard.setToX(0.98);
        zoomDashboard.setToY(0.98);

        FadeTransition fadeIntro = new FadeTransition(Duration.millis(600), introRoot);
        fadeIntro.setFromValue(0.0);
        fadeIntro.setToValue(1.0);

        ScaleTransition zoomIntro = new ScaleTransition(Duration.millis(600), introRoot);
        zoomIntro.setFromX(1.08);
        zoomIntro.setFromY(1.08);
        zoomIntro.setToX(1.0);
        zoomIntro.setToY(1.0);

        ParallelTransition transition = new ParallelTransition(
                fadeDashboard,
                zoomDashboard,
                fadeIntro,
                zoomIntro
        );

        transition.setOnFinished(event -> {
            dashboardRoot.setDisable(true);
            btnVoltarInicio.setVisible(false);
            btnVoltarInicio.setManaged(false);
        });

        transition.play();
    }

    private void iniciarSimulacao() {
        if (currentEngine != null) {
            return;
        }

        config = SimulationConfig.defaultConfig().withAgentAmounts(
                lerInteiro(txtSimpleReaders, 3),
                lerInteiro(txtCriticalReaders, 2),
                lerInteiro(txtWriters, 2),
                lerInteiro(txtCriticalWriters, 1)
        );

        logTextArea.clear();
        filaListView.getItems().clear();
        metricasVBox.getChildren().clear();
        mesasDeLivros.clear();
        atualizarCenaBiblioteca3D();

        eventBus = new EventBus();
        eventBus.addObserver(this);

        int selectedIndex = cenarioComboBox.getSelectionModel().getSelectedIndex();

        switch (selectedIndex) {
            case 0 -> currentEngine = SimulationEngine.defaultScenario(config, eventBus);
            case 1 -> currentEngine = SimulationEngine.libraryScenario(config, eventBus);
            case 2 -> currentEngine = SimulationEngine.chaosScenario(config, eventBus);
            case 3 -> currentEngine = SimulationEngine.deadlockScenario(config, eventBus);
            case 4 -> currentEngine = SimulationEngine.starvationScenario(config, eventBus);
            default -> currentEngine = SimulationEngine.defaultScenario(config, eventBus);
        }

        aplicarEstiloBotaoIniciarRodando();

        btnParar.setDisable(false);
        cenarioComboBox.setDisable(true);
        bloquearCamposConfiguracao(true);

        Thread engineThread = new Thread(currentEngine::start, "Thread-Engine-Principal");
        engineThread.setDaemon(true);
        engineThread.start();
    }

    private void pararSimulacao() {
        SimulationEngine engineToStop = currentEngine;

        if (engineToStop == null) {
            resetarControles();
            return;
        }

        btnParar.setDisable(true);

        Thread stopThread = new Thread(() -> {
            engineToStop.stop();
            String relatorio = engineToStop.metricsReport();

            Platform.runLater(() -> {
                logTextArea.appendText("\n" + relatorio + "\n");
                currentEngine = null;
                resetarControles();
            });
        }, "Thread-Parada-Simulacao");

        stopThread.setDaemon(true);
        stopThread.start();
    }

    private void resetarControles() {
        aplicarEstiloBotaoIniciarNormal();

        btnParar.setDisable(true);
        cenarioComboBox.setDisable(false);
        bloquearCamposConfiguracao(false);
    }

    private void aplicarEstiloBotaoIniciarNormal() {
        btnIniciar.setDisable(false);
        btnIniciar.setText("▶");
        btnIniciar.setStyle(
                "-fx-background-color: #22c55e;" +
                        "-fx-text-fill: #052e16;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
    }

    private void aplicarEstiloBotaoIniciarRodando() {
        btnIniciar.setDisable(false);
        btnIniciar.setText("⏳");
        btnIniciar.setStyle(
                "-fx-background-color: #475569;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: default;"
        );
    }

    private void bloquearCamposConfiguracao(boolean bloquear) {
        txtSimpleReaders.setDisable(bloquear);
        txtCriticalReaders.setDisable(bloquear);
        txtWriters.setDisable(bloquear);
        txtCriticalWriters.setDisable(bloquear);
    }

    private int lerInteiro(TextField field, int fallback) {
        try {
            int value = Integer.parseInt(field.getText().trim());

            if (value < 0) {
                field.setText(String.valueOf(fallback));
                return fallback;
            }

            return value;
        } catch (Exception ignored) {
            field.setText(String.valueOf(fallback));
            return fallback;
        }
    }

    @Override
    public void onEvent(SimulationEvent event) {
        Platform.runLater(() -> {
            try {
                String time = TIME_FORMATTER.format(event.timestamp().atZone(ZoneId.systemDefault()));
                String processName = event.process() == null ? "[SISTEMA]" : event.process().label();

                logTextArea.appendText(String.format("[%s] %-32s %s\n", time, processName, event.message()));
                limitarLog();

                if (event.process() != null && event.state() != null) {
                    atualizarFilaVisual(event.process(), event.state());

                    String msgSegura = event.message() != null ? event.message() : "";
                    atualizarEstadoBiblioteca(event.process(), event.state(), msgSegura);
                }

                if (event.snapshot() != null) {
                    atualizarMetricasDireita(event.snapshot());
                }
            } catch (Exception e) {
                System.err.println("Erro ao renderizar evento: " + e.getMessage());
            }
        });
    }

    private void limitarLog() {
        int limite = 30_000;

        if (logTextArea.getLength() > limite) {
            logTextArea.deleteText(0, 8_000);
        }
    }

    private void atualizarFilaVisual(AccessRequest process, MageState state) {
        filaListView.getItems().removeIf(item -> item.contains(process.label()));

        if (state == MageState.RESTING || state == MageState.STOPPED) {
            return;
        }

        String sigla = switch (process.role()) {
            case SIMPLE_CONSULTATION -> "[CS]";
            case CRITICAL_RESEARCH -> "[PC]";
            case MAGICAL_RITUAL -> "[RM]";
            case CRITICAL_RITUAL -> "[RC]";
        };

        String status = state == MageState.WAITING
                ? "⏳"
                : state == MageState.WRITING
                  ? "✍️"
                  : "📖";

        filaListView.getItems().add(String.format("%s %s %s", status, sigla, process.label()));

        while (filaListView.getItems().size() > 250) {
            filaListView.getItems().remove(0);
        }
    }

    private void atualizarEstadoBiblioteca(AccessRequest process, MageState state, String message) {
        if (state == MageState.READING || state == MageState.WRITING) {
            String nomeLivro = extrairNomeLivro(message);

            mesasDeLivros.putIfAbsent(nomeLivro, new LinkedHashMap<>());
            mesasDeLivros.get(nomeLivro).put(process.label(), new ProcessInfo(process, state));
        } else {
            for (Map<String, ProcessInfo> magosNoLivro : mesasDeLivros.values()) {
                magosNoLivro.remove(process.label());
            }

            mesasDeLivros.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        atualizarCenaBiblioteca3D();
    }

    private String extrairNomeLivro(String message) {
        String fallback = "Grimório Central";

        if (message == null || !message.contains("'")) {
            return fallback;
        }

        int start = message.indexOf("'") + 1;
        int end = message.indexOf("'", start);

        if (start > 0 && end > start) {
            return message.substring(start, end);
        }

        return fallback;
    }

    private void montarPortal3D() {
        Group portalWorld = new Group();

        AmbientLight ambient = new AmbientLight(Color.web("#64748b"));

        PointLight cyanLight = new PointLight(Color.web("#22d3ee"));
        cyanLight.setTranslateX(-220);
        cyanLight.setTranslateY(-180);
        cyanLight.setTranslateZ(-220);

        PointLight goldLight = new PointLight(Color.web("#eab308"));
        goldLight.setTranslateX(220);
        goldLight.setTranslateY(-120);
        goldLight.setTranslateZ(-160);

        Group ringOuter = criarAnelDeEsferas(190, 44, 5, Color.web("#22d3ee"));
        Group ringInner = criarAnelDeEsferas(125, 32, 4, Color.web("#8b5cf6"));

        Group book = criarLivro3D(1.45);
        book.setTranslateY(20);
        book.setTranslateZ(40);

        RotateTransition introBookRotation = new RotateTransition(Duration.seconds(7), book);
        introBookRotation.setAxis(Rotate.Y_AXIS);
        introBookRotation.setByAngle(360);
        introBookRotation.setCycleCount(Animation.INDEFINITE);
        introBookRotation.play();

        RotateTransition ringRotation = new RotateTransition(Duration.seconds(12), ringOuter);
        ringRotation.setAxis(Rotate.Z_AXIS);
        ringRotation.setByAngle(360);
        ringRotation.setCycleCount(Animation.INDEFINITE);
        ringRotation.play();

        RotateTransition ringInnerRotation = new RotateTransition(Duration.seconds(9), ringInner);
        ringInnerRotation.setAxis(Rotate.Z_AXIS);
        ringInnerRotation.setByAngle(-360);
        ringInnerRotation.setCycleCount(Animation.INDEFINITE);
        ringInnerRotation.play();

        portalWorld.getChildren().addAll(
                ambient,
                cyanLight,
                goldLight,
                ringOuter,
                ringInner,
                book
        );

        portalWorld.getTransforms().add(new Rotate(-10, Rotate.X_AXIS));

        SubScene subScene = criarSubScene(portalWorld, intro3dContainer, -760);
        intro3dContainer.getChildren().setAll(subScene);
    }

    private void montarBiblioteca3D() {
        Group libraryWorld = new Group();

        activeBooksLayer = new Group();
        activeAgentsLayer = new Group();

        AmbientLight ambient = new AmbientLight(Color.web("#cbd5e1"));

        PointLight mainLight = new PointLight(Color.web("#eab308"));
        mainLight.setTranslateX(-180);
        mainLight.setTranslateY(-220);
        mainLight.setTranslateZ(-220);

        PointLight blueLight = new PointLight(Color.web("#22d3ee"));
        blueLight.setTranslateX(220);
        blueLight.setTranslateY(-180);
        blueLight.setTranslateZ(-190);

        libraryWorld.getChildren().addAll(
                ambient,
                mainLight,
                blueLight,
                activeBooksLayer,
                activeAgentsLayer
        );

        libraryWorld.getTransforms().add(new Rotate(-10, Rotate.X_AXIS));

        SubScene subScene = criarSubScene(libraryWorld, libraryViewport, -820);

        aplicarFundoBibliotecaComoCss();

        Rectangle escurecerFundo = new Rectangle();
        escurecerFundo.widthProperty().bind(libraryViewport.widthProperty());
        escurecerFundo.heightProperty().bind(libraryViewport.heightProperty());
        escurecerFundo.setFill(Color.web("#020617", 0.08));
        escurecerFundo.setMouseTransparent(true);

        libraryViewport.getChildren().setAll(
                escurecerFundo,
                subScene
        );
    }

    private void aplicarFundoBibliotecaComoCss() {
        URL imageUrl = getClass().getResource(
                "/br/edu/ifsuldeminas/rafael/arcanelibrary/ui/gui/assets/biblioteca_arcana_fundo.png"
        );

        if (imageUrl != null) {
            libraryViewport.setStyle(
                    "-fx-background-color: #020617;" +
                            "-fx-background-image: url(\"" + imageUrl.toExternalForm() + "\");" +
                            "-fx-background-size: cover;" +
                            "-fx-background-position: center center;" +
                            "-fx-background-repeat: no-repeat;"
            );
        } else {
            libraryViewport.setStyle(
                    "-fx-background-color: radial-gradient(center 50% 45%, radius 80%, #1e293b 0%, #0f172a 45%, #020617 100%);"
            );
        }
    }

    private SubScene criarSubScene(Group world, StackPane container, double cameraZ) {
        SubScene subScene = new SubScene(world, 900, 650, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setMouseTransparent(true);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(cameraZ);
        camera.setTranslateY(-35);
        camera.setNearClip(0.1);
        camera.setFarClip(5_000);
        camera.setFieldOfView(42);

        subScene.setCamera(camera);
        subScene.widthProperty().bind(container.widthProperty());
        subScene.heightProperty().bind(container.heightProperty());

        return subScene;
    }

    private Group criarLivro3D(double escala) {
        Group book = new Group();

        Box cover = new Box(150 * escala, 18 * escala, 105 * escala);
        cover.setMaterial(material("#4c1d95"));

        Box pages = new Box(128 * escala, 12 * escala, 88 * escala);
        pages.setTranslateY(-3 * escala);
        pages.setTranslateX(8 * escala);
        pages.setMaterial(material("#f8fafc"));

        Box spine = new Box(20 * escala, 24 * escala, 112 * escala);
        spine.setTranslateX(-78 * escala);
        spine.setMaterial(material("#7f1d1d"));

        Box goldLine = new Box(8 * escala, 26 * escala, 115 * escala);
        goldLine.setTranslateX(-48 * escala);
        goldLine.setMaterial(material("#eab308"));

        Sphere gem = new Sphere(10 * escala);
        gem.setTranslateY(-16 * escala);
        gem.setTranslateZ(-2 * escala);
        gem.setMaterial(material("#22d3ee"));

        book.getChildren().addAll(cover, pages, spine, goldLine, gem);
        return book;
    }

    private Group criarLivroFlutuante3D(double escala) {
        Group livroCentral = criarLivro3D(escala);
        livroCentral.setTranslateY(0);
        livroCentral.setTranslateZ(0);

        RotateTransition giroLivro = new RotateTransition(Duration.seconds(8), livroCentral);
        giroLivro.setAxis(Rotate.Y_AXIS);
        giroLivro.setByAngle(360);
        giroLivro.setCycleCount(Animation.INDEFINITE);
        giroLivro.play();

        Group anelEnergia = criarAnelDeEsferas(95 * escala, 30, 3.2 * escala, Color.web("#22d3ee"));
        anelEnergia.setTranslateY(18 * escala);
        anelEnergia.setTranslateZ(0);
        anelEnergia.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        RotateTransition giroAnel = new RotateTransition(Duration.seconds(7), anelEnergia);
        giroAnel.setAxis(Rotate.Y_AXIS);
        giroAnel.setByAngle(360);
        giroAnel.setCycleCount(Animation.INDEFINITE);
        giroAnel.play();

        Group livroFlutuante = new Group();
        livroFlutuante.getChildren().addAll(anelEnergia, livroCentral);

        TranslateTransition flutuarLivro = new TranslateTransition(Duration.seconds(2.2), livroFlutuante);
        flutuarLivro.setFromY(-35);
        flutuarLivro.setToY(-12);
        flutuarLivro.setAutoReverse(true);
        flutuarLivro.setCycleCount(Animation.INDEFINITE);
        flutuarLivro.play();

        return livroFlutuante;
    }

    private Group criarMago3D(AccessRequest process, MageState state, double escala) {
        Group mage = new Group();

        Color cor = corPorTipo(process.role());

        Cylinder body = new Cylinder(12 * escala, 42 * escala);
        body.setTranslateY(0);
        body.setMaterial(new PhongMaterial(cor));

        Sphere head = new Sphere(12 * escala);
        head.setTranslateY(-32 * escala);
        head.setMaterial(material("#e5e7eb"));

        Cylinder base = new Cylinder(18 * escala, 7 * escala);
        base.setTranslateY(25 * escala);
        base.setMaterial(material("#020617"));

        Sphere orb = new Sphere((state == MageState.WRITING ? 8 : 5) * escala);
        orb.setTranslateY(-58 * escala);
        orb.setMaterial(new PhongMaterial(cor.brighter()));

        PointLight personalLight = new PointLight(cor);
        personalLight.setTranslateY(-65 * escala);
        personalLight.setTranslateZ(-25 * escala);

        mage.getChildren().addAll(base, body, head, orb, personalLight);
        return mage;
    }

    private Group criarAnelDeEsferas(double radius, int count, double sphereRadius, Color color) {
        Group ring = new Group();
        PhongMaterial mat = new PhongMaterial(color);

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;

            Sphere sphere = new Sphere(sphereRadius);
            sphere.setTranslateX(radius * Math.cos(angle));
            sphere.setTranslateY(radius * Math.sin(angle));
            sphere.setMaterial(mat);

            ring.getChildren().add(sphere);
        }

        return ring;
    }

    private void atualizarCenaBiblioteca3D() {
        if (activeBooksLayer == null || activeAgentsLayer == null) {
            return;
        }

        activeBooksLayer.getChildren().clear();
        activeAgentsLayer.getChildren().clear();

        int qtdLivros = mesasDeLivros.size();

        if (qtdLivros == 0) {
            Group livroIdle = criarLivroFlutuante3D(1.05);
            livroIdle.setTranslateX(0);
            livroIdle.setTranslateY(15);
            livroIdle.setTranslateZ(0);

            activeBooksLayer.getChildren().add(livroIdle);

            lblSceneInfo.setText("Livro arcano flutuando aguardando agentes");
            return;
        }

        int totalAtivos = mesasDeLivros.values()
                .stream()
                .mapToInt(Map::size)
                .sum();

        int livroIndex = 0;
        int totalVisualizados = 0;
        int limiteTotal = 48;

        for (Map.Entry<String, Map<String, ProcessInfo>> entradaLivro : mesasDeLivros.entrySet()) {
            Map<String, ProcessInfo> agentesDoLivro = entradaLivro.getValue();

            double bookX;
            double bookY;
            double bookZ;
            double escalaLivro;
            double raioMagos;
            double escalaMago;

            if (qtdLivros == 1) {
                bookX = 0;
                bookY = 15;
                bookZ = 0;
                escalaLivro = 1.05;
                raioMagos = 205;
                escalaMago = 1.0;
            } else if (qtdLivros == 2) {
                bookX = livroIndex == 0 ? -190 : 190;
                bookY = 25;
                bookZ = 10;
                escalaLivro = 0.82;
                raioMagos = 115;
                escalaMago = 0.76;
            } else {
                if (livroIndex == 0) {
                    bookX = -245;
                    bookZ = 35;
                } else if (livroIndex == 1) {
                    bookX = 0;
                    bookZ = -25;
                } else if (livroIndex == 2) {
                    bookX = 245;
                    bookZ = 35;
                } else {
                    double angleBook = 2 * Math.PI * livroIndex / qtdLivros;
                    bookX = 255 * Math.cos(angleBook);
                    bookZ = 35 + 120 * Math.sin(angleBook);
                }

                bookY = 30;
                escalaLivro = 0.66;
                raioMagos = 90;
                escalaMago = 0.62;
            }

            Group livroVisual = criarLivroFlutuante3D(escalaLivro);
            livroVisual.setTranslateX(bookX);
            livroVisual.setTranslateY(bookY);
            livroVisual.setTranslateZ(bookZ);

            activeBooksLayer.getChildren().add(livroVisual);

            List<ProcessInfo> agentesAtivos = new ArrayList<>(agentesDoLivro.values());

            int limitePorLivro = Math.min(
                    agentesAtivos.size(),
                    Math.max(4, limiteTotal / Math.max(qtdLivros, 1))
            );

            for (int i = 0; i < limitePorLivro; i++) {
                if (totalVisualizados >= limiteTotal) {
                    break;
                }

                ProcessInfo info = agentesAtivos.get(i);

                double angle = 2 * Math.PI * i / Math.max(limitePorLivro, 1);

                double x = bookX + raioMagos * Math.cos(angle);
                double z = bookZ - 20 + raioMagos * Math.sin(angle);

                Group mage = criarMago3D(info.process(), info.state(), escalaMago);

                mage.setTranslateX(x);
                mage.setTranslateY(95);
                mage.setTranslateZ(z);

                activeAgentsLayer.getChildren().add(mage);
                totalVisualizados++;
            }

            livroIndex++;
        }

        if (totalAtivos > totalVisualizados) {
            lblSceneInfo.setText("Mostrando " + totalVisualizados + " de " + totalAtivos + " agentes entre " + qtdLivros + " grimórios");
        } else if (qtdLivros == 1) {
            lblSceneInfo.setText(totalAtivos + " agente(s) ao redor do livro arcano");
        } else {
            lblSceneInfo.setText(totalAtivos + " agente(s) distribuído(s) entre " + qtdLivros + " grimórios");
        }
    }

    private Color corPorTipo(MageAccessType type) {
        return switch (type) {
            case SIMPLE_CONSULTATION -> Color.web("#22d3ee");
            case CRITICAL_RESEARCH -> Color.web("#a855f7");
            case MAGICAL_RITUAL -> Color.web("#ef4444");
            case CRITICAL_RITUAL -> Color.web("#eab308");
        };
    }

    private PhongMaterial material(String color) {
        return new PhongMaterial(Color.web(color));
    }

    private void atualizarMetricasDireita(SynchronizationSnapshot snap) {
        metricasVBox.getChildren().clear();

        VBox cardAcesso = criarCartaoBase("ACESSO ATUAL");
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        if (snap.writerActive()) {
            lblStatus.setText("⛔ RITUAL ATIVO");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #ef4444;");
        } else if (snap.activeReaders() > 0) {
            lblStatus.setText("📖 " + snap.activeReaders() + " CONSULTA(S)");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #22d3ee;");
        } else {
            lblStatus.setText("✅ GRIMÓRIO LIVRE");
            lblStatus.setStyle(lblStatus.getStyle() + "-fx-text-fill: #22c55e;");
        }

        cardAcesso.getChildren().add(lblStatus);

        VBox cardFila = criarCartaoBase("FILAS");
        cardFila.getChildren().addAll(
                criarTextoInfo("Rituais: ", snap.waitingWriters(), "#ef4444"),
                criarTextoInfo("Pesquisas críticas: ", snap.waitingCriticalReaders(), "#a855f7"),
                criarTextoInfo("Consultas: ", snap.waitingCommonReaders(), "#22d3ee")
        );

        VBox cardMotor = criarCartaoBase("MOTOR");
        cardMotor.getChildren().addAll(
                criarTextoInfo("Lote consultas: ", snap.commonReaderBatchQuota(), "#e5e7eb"),
                criarTextoInfo("Burst crítico: ", snap.criticalVipBurst() + "/" + snap.maxCriticalVipBurst(), "#eab308"),
                criarTextoInfo("Leituras concluídas: ", (int) snap.completedReads(), "#86efac"),
                criarTextoInfo("Escritas concluídas: ", (int) snap.completedWrites(), "#fb923c")
        );

        VBox cardConfig = criarCartaoBase("CONFIGURAÇÃO");
        cardConfig.getChildren().addAll(
                criarTextoInfo("Consultas: ", config.simpleReaders(), "#22d3ee"),
                criarTextoInfo("Críticos: ", config.criticalReaders(), "#a855f7"),
                criarTextoInfo("Rituais: ", config.writers(), "#ef4444"),
                criarTextoInfo("Rituais críticos: ", config.criticalWriters(), "#eab308")
        );

        metricasVBox.getChildren().addAll(cardAcesso, cardFila, cardMotor, cardConfig);
    }

    private void prepararMetricasIniciais() {
        metricasVBox.getChildren().clear();

        VBox card = criarCartaoBase("STATUS");
        Label texto = new Label("Aguardando início da simulação.");
        texto.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        card.getChildren().add(texto);
        metricasVBox.getChildren().add(card);
    }

    private VBox criarCartaoBase(String titulo) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1e293b, #111827);" +
                        "-fx-padding: 9;" +
                        "-fx-background-radius: 9;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 9;" +
                        "-fx-border-width: 1;"
        );

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #eab308; -fx-font-size: 10px; -fx-font-weight: bold;");

        box.getChildren().add(lblTitulo);
        return box;
    }

    private Label criarTextoInfo(String label, int valor, String corHex) {
        Label l = new Label(label + valor);
        l.setStyle("-fx-text-fill: " + corHex + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private Label criarTextoInfo(String label, String valor, String corHex) {
        Label l = new Label(label + valor);
        l.setStyle("-fx-text-fill: " + corHex + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }
}