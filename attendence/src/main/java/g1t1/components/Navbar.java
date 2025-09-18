package g1t1.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class Navbar extends AnchorPane {
    public Navbar() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NavBar.fxml"));
        // Cannot declare controller in fxml file since navbar
        // is used in multiple places
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
