package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.config.RememberMeManager;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.logger.AppLogger;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    private CheckBox cbxRememberMe;

    @FXML
    public void goToRegister() {
        Router.changePage(PageName.Register);
    }

    @FXML
    public void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            Router.changePage(PageName.PastRecords);
        });

        // Load remembered credentials if available
        RememberMeManager rememberMeManager = RememberMeManager.getInstance();
        if (rememberMeManager.hasRememberedCredentials()) {
            emailInput.textProperty().set(rememberMeManager.getRememberedEmail());
            passwordInput.textProperty().set(rememberMeManager.getRememberedPassword());
            cbxRememberMe.setSelected(true);
            AppLogger.log("Auto-filled login credentials from remember me");
        }
    }

    @FXML
    public void login() {
        String email = emailInput.getText();
        String password = passwordInput.getText();
        boolean success = AuthenticationContext.loginTeacher(email, password);

        if (success) {
            // Handle remember me
            RememberMeManager rememberMeManager = RememberMeManager.getInstance();
            if (cbxRememberMe.isSelected()) {
                rememberMeManager.rememberCredentials(email, password);
                AppLogger.logf("Remember me enabled for user: %s", email);
            } else {
                rememberMeManager.forgetCredentials();
                AppLogger.log("Remember me disabled - credentials cleared");
            }

            Toast.show("Logged in!", Toast.ToastType.SUCCESS);
        } else {
            lblInvalidCredentials.setVisible(true);
        }
    }
}
