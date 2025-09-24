package g1t1.models.scenes;

import g1t1.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class Page {
    private final Parent root;
    private final PageController controller;
    private final PageName pageName;

    // From root!!
    public Page(PageName pageName, String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.getInstance().getClass().getResource(path));
        this.root = loader.load();
        this.controller = loader.getController();
        this.pageName = pageName;
    }

    public Parent getRoot() {
        return this.root;
    }

    public PageController getController() {
        return this.controller;
    }

    public PageName getPageName() {
        return this.pageName;
    }
}
