package g1t1.scenes;

import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import javafx.fxml.FXML;

public class StartSessionViewController extends PageController {
    @FXML
    public void startSession() {
        Router.changePage(PageName.DuringSession);
    }
}
