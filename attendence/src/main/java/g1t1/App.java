package g1t1;

import g1t1.models.scenes.Page;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.OnNavigateEvent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nu.pattern.OpenCV;

public class App extends Application {
    public static final int WIDTH = 1440;
    public static final int HEIGHT = 750;
    private static final PageName initialPage = PageName.PastRecords;

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

        scene.getStylesheets().add(getClass().getResource("css/app.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("css/button.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("css/stepper.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("css/tabs.css").toExternalForm());

        Router.changePage(initialPage);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        // Listen to router change events and navigate accordingly
        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            instance.currentScene.setRoot(e.getNewPage().getRoot());
        });
    }
}