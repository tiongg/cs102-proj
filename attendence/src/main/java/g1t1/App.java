package g1t1;

import g1t1.models.scenes.Page;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends Application {
    public static final int WIDTH = 1440;
    public static final int HEIGHT = 750;
    private static final PageName initialPage = PageName.Login;

    private static App instance;
    private Scene currentScene;

    public static App getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        launch();
    }

    @Override
    public void start(Stage stage) {
        App.instance = this;
        Router.initialize();

        Page basePage = Router.scenes.get(initialPage);
        Scene scene = new Scene(basePage.getRoot(), WIDTH, HEIGHT);
        instance.currentScene = scene;
        
        loadCss(scene);
        Router.changePage(initialPage);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void init() {
        // Listen to router change events and navigate accordingly
        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            instance.currentScene.setRoot(e.newPage().getRoot());
        });
    }

    private void loadCss(Scene scene) {
        try {
            URL cssFolderUrl = getClass().getResource("css/");
            if (cssFolderUrl != null) {
                Path cssFolderPath = Paths.get(cssFolderUrl.toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(cssFolderPath, "*.css")) {
                    for (Path file : stream) {
                        String cssFilePath = file.toUri().toURL().toExternalForm();
                        scene.getStylesheets().add(cssFilePath);
                    }
                }
            } else {
                System.err.println("CSS folder not found in resources.");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS files: " + e.getMessage());
        }
    }
}