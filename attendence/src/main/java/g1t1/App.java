package g1t1;

import g1t1.models.scenes.Page;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public class App extends Application {
    public static int WIDTH = 1440;
    public static int HEIGHT = 700;

    private static App instance;
    private final HashMap<Page, Parent> scenes = new HashMap<>();
    private Scene currentScene;
    private Page currentPage;

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        launch();
    }

    private static void loadScenes() {
        Map<Page, String> scenePaths = Map.ofEntries(
                entry(Page.PastRecords, "scenes/ReportsView.fxml"),
                entry(Page.MyClasses, "scenes/MyClassesView.fxml"),
                entry(Page.Onboard, "scenes/OnboardView.fxml"),
                entry(Page.StartSession, "scenes/StartSessionView.fxml"),
                entry(Page.Settings, "scenes/SettingsView.fxml")
        );

        scenePaths.forEach((key, value) -> {
            try {
                Parent page = FXMLLoader.load(instance.getClass().getResource(value));
                instance.scenes.put(key, page);
            } catch (Exception e) {
                System.err.println("Error loading " + key + " from " + value);
                e.printStackTrace();
            }
        });
    }

    public static void changePage(Page newPage) {
        Parent root = instance.scenes.get(newPage);
        instance.currentPage = newPage;
        instance.currentScene.setRoot(root);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(instance.scenes.get(Page.PastRecords), WIDTH, HEIGHT);
        instance.currentScene = scene;
        changePage(Page.PastRecords);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        App.instance = this;
        loadScenes();
    }
}