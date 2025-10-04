package g1t1.scenes;

import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController extends PageController {
    @FXML
    private TextField emailInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    public void goToRegister() {
        Router.changePage(PageName.Register);
    }

    @FXML
    public void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            Router.changePage(PageName.PastRecords);
        });
    }

    @FXML
    public void login() {
        AuthenticationContext.loginTeacher(emailInput.getText(), passwordInput.getText());
    }
}
