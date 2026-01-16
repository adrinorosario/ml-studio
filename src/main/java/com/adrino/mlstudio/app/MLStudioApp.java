package com.adrino.mlstudio.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MLStudioApp extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane(new Label("ML Studio is running 🚀"));
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("ML Studio");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
