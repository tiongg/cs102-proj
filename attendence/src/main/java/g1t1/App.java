package g1t1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        Label lblJavaVersion = new Label("Java version: " + javaVersion);
        Label lblJavaFxVersion = new Label("JavaFX version: " + javafxVersion);
        Label lblOpencvVersion = new Label("Opencv version: " + org.opencv.core.Core.VERSION);

        VBox vbox = new VBox(lblJavaVersion, lblJavaFxVersion, lblOpencvVersion);
        vbox.setSpacing(12);

        Scene scene = new Scene(new StackPane(vbox), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        compile error
        OpenCV.loadLocally();
        launch();
    }
}