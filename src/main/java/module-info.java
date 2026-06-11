module com.arcane.scriptorium {
    // 1. Avisamos ao Java que precisamos das bibliotecas do JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // 2. Exportamos o nosso novo pacote da interface gráfica
    exports com.arcane.scriptorium.ui.gui;

    // 3. O Pulo do Gato: Permite que o FXML injete os botões nas variáveis @FXML
    opens com.arcane.scriptorium.ui.gui to javafx.fxml;

    // Suas exportações originais (mantidas intactas)
    exports com.arcane.scriptorium;
    exports com.arcane.scriptorium.domain;
    exports com.arcane.scriptorium.events;
    exports com.arcane.scriptorium.simulation;
    exports com.arcane.scriptorium.synchronization;
    exports com.arcane.scriptorium.ui.console;
    exports com.arcane.scriptorium.validation;
}