package src.main.java.com.adrino.aurea_ml_studio.bootstrap;

import javafx.application.Application;
import javafx.stage.Stage;

public class ApplicationBootstrap extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Aurea ML Studio");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
