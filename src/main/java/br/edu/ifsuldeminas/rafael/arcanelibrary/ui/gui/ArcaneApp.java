package br.edu.ifsuldeminas.rafael.arcanelibrary.ui.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcaneApp extends Application {

    private static final double WINDOW_WIDTH = 1120;
    private static final double WINDOW_HEIGHT = 690;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ArcaneApp.class.getResource("main_view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, true);

        primaryStage.setTitle("ArcaneLibrary - Biblioteca Arcana 3D");
        primaryStage.setScene(scene);

        primaryStage.setResizable(false);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);

        primaryStage.show();
        centralizarJanela(primaryStage);
    }

    private void centralizarJanela(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        double x = bounds.getMinX() + (bounds.getWidth() - WINDOW_WIDTH) / 2.0;
        double y = bounds.getMinY() + (bounds.getHeight() - WINDOW_HEIGHT) / 2.0;

        stage.setX(Math.max(bounds.getMinX(), x));
        stage.setY(Math.max(bounds.getMinY(), y));
    }

    public static void main(String[] args) {
        launch(args);
    }
}