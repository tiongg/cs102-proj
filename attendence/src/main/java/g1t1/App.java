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
    public static int WIDTH = 1440;
    public static int HEIGHT = 700;

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
        Page pastRecordsPage = Router.scenes.get(PageName.PastRecords);
        Scene scene = new Scene(pastRecordsPage.getRoot(), WIDTH, HEIGHT);
        instance.currentScene = scene;

        Router.changePage(PageName.PastRecords);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void init() {
        App.instance = this;
        Router.initialize();

        // Listen to router change events and navigate accordingly
        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            instance.currentScene.setRoot(e.getNewPage().getRoot());
        });
    }
}