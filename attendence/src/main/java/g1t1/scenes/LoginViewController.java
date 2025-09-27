package g1t1.scenes;

import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import javafx.fxml.FXML;

public class LoginViewController extends PageController {

    @FXML
    public void goToRegister() {
        Router.changePage(PageName.Register);
    }
}
