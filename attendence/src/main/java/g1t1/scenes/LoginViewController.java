package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController extends PageController {
    @FXML
    private TextField emailInput;

    @FXML
    private PasswordField passwordInput;

    @FXML
    private Label lblInvalidCredentials;

    @FXML
    public void goToRegister() {
        Router.changePage(PageName.Register);
    }

    @FXML
    public void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            Router.changePage(PageName.PastRecords);
        });

        emailInput.textProperty().set("a@a.com");
        passwordInput.textProperty().set("123");
    }

    @FXML
    public void login() {
        boolean success = AuthenticationContext.loginTeacher(emailInput.getText(), passwordInput.getText());
        if (success) {
            Toast.show("Logged in!", Toast.ToastType.SUCCESS);
        } else {
            lblInvalidCredentials.setVisible(true);
        }
    }
}
