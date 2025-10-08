package g1t1;

import g1t1.db.DatabaseInitializer;
import g1t1.models.scenes.Page;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

import java.net.URL;

public class App extends Application {
    public static final int WIDTH = 1440;
    public static final int HEIGHT = 750;
    private static final PageName initialPage = PageName.Login;

    private static App instance;
    private Scene currentScene;
    private Stage currentStage;

    public static App getInstance() {
        return instance;
    }

    public static Stage getRootStage() {
        return getInstance().currentStage;
    }

    public static void main(String[] args) {
        OpenCV.loadLocally();
        DatabaseInitializer db = new DatabaseInitializer();
        db.init();
        launch();
    }

    @Override
    public void start(Stage stage) {
        App.instance = this;
        instance.currentStage = stage;
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
        String[] cssFiles = {
                "app.css", "button.css", "date-picker.css", "stepper.css",
                "tabs.css", "toast.css"
        };

        for (String cssFile : cssFiles) {
            URL url = getClass().getResource(String.format("css/%s", cssFile));
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            } else {
                System.err.println("CSS not found: " + cssFile);
            }
        }
    }
}