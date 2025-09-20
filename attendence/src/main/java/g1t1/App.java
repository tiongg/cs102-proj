package g1t1;

import g1t1.models.scenes.Page;
import g1t1.models.scenes.PageName;
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
    private final HashMap<PageName, Page> scenes = new HashMap<>();
    private Scene currentScene;
    private PageName currentPageName;

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        launch();
    }

    private static void loadScenes() {
        Map<PageName, String> scenePaths = Map.ofEntries(
                entry(PageName.PastRecords, "scenes/ReportsView.fxml"),
                entry(PageName.MyClasses, "scenes/MyClassesView.fxml"),
                entry(PageName.Onboard, "scenes/OnboardView.fxml"),
                entry(PageName.StartSession, "scenes/StartSessionView.fxml"),
                entry(PageName.Settings, "scenes/SettingsView.fxml")
        );

        scenePaths.forEach((key, value) -> {
            try {
                Page page = new Page(value);
                instance.scenes.put(key, page);
            } catch (Exception e) {
                System.err.println("Error loading " + key + " from " + value);
                e.printStackTrace();
            }
        });
    }

    public static void changePage(PageName newPageName) {
        Page page = instance.scenes.get(newPageName);
        instance.currentPageName = newPageName;
        instance.currentScene.setRoot(page.getRoot());
        page.getController().onMount();
    }

    @Override
    public void start(Stage stage) {
        Page pastRecordsPage = instance.scenes.get(PageName.PastRecords);
        Scene scene = new Scene(pastRecordsPage.getRoot(), WIDTH, HEIGHT);
        instance.currentScene = scene;
        changePage(PageName.PastRecords);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        App.instance = this;
        loadScenes();
    }
}