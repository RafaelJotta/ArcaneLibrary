module br.edu.ifsuldeminas.rafael.arcanelibrary {
    // 1. Avisamos ao Java que precisamos das bibliotecas do JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // 2. Exportamos o nosso novo pacote da interface gráfica
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.ui.gui;

    // 3. O Pulo do Gato: Permite que o FXML injete os botões nas variáveis @FXML
    opens br.edu.ifsuldeminas.rafael.arcanelibrary.ui.gui to javafx.fxml;

    // Suas exportações originais (mantidas intactas)
    exports br.edu.ifsuldeminas.rafael.arcanelibrary;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.domain;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.events;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.simulation;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.synchronization;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.ui.console;
    exports br.edu.ifsuldeminas.rafael.arcanelibrary.validation;
}