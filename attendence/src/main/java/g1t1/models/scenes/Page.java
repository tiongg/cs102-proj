package g1t1.models.scenes;

import g1t1.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class Page {
    private Parent root;
    private PageController controller;

    // From root!!
    public Page(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.getInstance().getClass().getResource(path));
        this.root = loader.load();
        this.controller = loader.getController();
    }

    public Parent getRoot() {
        return this.root;
    }

    public PageController getController() {
        return this.controller;
    }
}
