package com.br.devsami;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MenuPrincipal.fxml")));
                Scene scene = new Scene(root, 800, 600);

                primaryStage.setTitle("TotemMind");
                primaryStage.setScene(scene);
                primaryStage.show();
        }

        public static void main(String[] args) {
                launch(args);
        }
}