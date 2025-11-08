package g1t1;

import g1t1.db.DatabaseInitializer;
import g1t1.features.logger.AppLogger;
import g1t1.features.logger.LogLevel;
import g1t1.models.scenes.Page;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
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
        AppLogger.log("=== Application Starting ===");
        AppLogger.logf("Java Version: %s", System.getProperty("java.version"));
        AppLogger.logf("OS: %s %s", System.getProperty("os.name"), System.getProperty("os.version"));

        try {
            AppLogger.log("Loading OpenCV library...");
            OpenCV.loadLocally();
            AppLogger.log("OpenCV loaded successfully");
        } catch (Exception e) {
            AppLogger.logf(LogLevel.Error, "Failed to load OpenCV: %s", e.getMessage());
            throw e;
        }

        try {
            AppLogger.log("Initializing database...");
            DatabaseInitializer db = new DatabaseInitializer();
            db.init();
            AppLogger.log("Database initialized successfully");
        } catch (Exception e) {
            AppLogger.logf(LogLevel.Error, "Failed to initialize database: %s", e.getMessage());
            throw e;
        }

        AppLogger.log("Launching JavaFX application...");
        launch();
    }

    @Override
    public void start(Stage stage) {
        AppLogger.log("JavaFX application started");
        App.instance = this;
        instance.currentStage = stage;

        AppLogger.log("Initializing router...");
        Router.initialize();

        Page basePage = Router.scenes.get(initialPage);
        Scene scene = new Scene(basePage.getRoot(), WIDTH, HEIGHT);
        instance.currentScene = scene;

        AppLogger.log("Loading fonts and stylesheets...");
        loadFonts();
        loadCss(scene);

        AppLogger.logf("Navigating to initial page: %s", initialPage);
        Router.changePage(initialPage);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        AppLogger.log("=== Application Ready ===");
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
                "tabs.css", "toast.css", "table.css", "student-list.css"
        };

        int loadedCount = 0;
        for (String cssFile : cssFiles) {
            URL url = getClass().getResource(String.format("css/%s", cssFile));
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
                loadedCount++;
            } else {
                AppLogger.logf(LogLevel.Warning, "CSS file not found: %s", cssFile);
                System.err.println("CSS not found: " + cssFile);
            }
        }
        AppLogger.logf("Loaded %d/%d CSS stylesheets", loadedCount, cssFiles.length);
    }

    private void loadFonts() {
        String[] variants = {
                "Black", "Bold", "ExtraBold", "ExtraLight", "Light",
                "Medium", "Regular", "SemiBold", "Thin"
        };
        int loadedCount = 0;
        for (String variant : variants) {
            try {
                Font font = Font.loadFont(getClass().getResourceAsStream(String.format("/fonts/Inter_18pt-%s.ttf", variant)), 18);
                if (font != null) {
                    loadedCount++;
                }
            } catch (Exception e) {
                AppLogger.logf(LogLevel.Warning, "Failed to load font variant: %s - %s", variant, e.getMessage());
            }
        }
        AppLogger.logf("Loaded %d/%d font variants", loadedCount, variants.length);
    }
}