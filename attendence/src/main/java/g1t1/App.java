package g1t1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

public class App extends Application {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
//            Parent root = FXMLLoader.load(getClass().getResource("DebugView.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("scenes/ReportsView.fxml"));
            Scene scene = new Scene(root, 1440, 700);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}