package g1t1;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.bytedeco.opencv.global.opencv_core;

import g1t1.models.Student;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        Label lblJavaVersion = new Label("Java version: " + javaVersion);
        Label lblJavaFxVersion = new Label("JavaFX version: " + javafxVersion);
        Label lblOpencvVersion = new Label("Opencv version: " + opencv_core.CV_VERSION);
        Scene scene = new Scene(new StackPane(lblJavaVersion, lblJavaFxVersion, lblOpencvVersion), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Student s = new Student("Jeff");
        System.out.println(s);
        Loader.load(opencv_java.class);
        launch();
    }
}