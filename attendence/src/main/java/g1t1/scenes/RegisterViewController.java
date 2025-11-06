package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.logger.AppLogger;
import g1t1.features.logger.LogLevel;
import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.users.RegisterTeacher;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

public class RegisterViewController extends PageController {
    private final BooleanProperty canNext = new SimpleBooleanProperty(false);

    private RegisterTeacher registerTeacher;

    @FXML
    private StepperControl stepper;
    @FXML
    private TabPane tabs;
    @FXML
    private Label backText;
    @FXML
    private Label nextText;
    @FXML
    private HBox backButton;
    @FXML
    private HBox nextButton;

    @FXML
    public void initialize() {
        stepper.currentIndexProperty().addListener((obv, oldIndex, newIndex) -> {
            if (stepper.isLast()) {
                nextText.setText("Login!");
            } else {
                nextText.setText("Next");
            }

            if (newIndex.intValue() == 0) {
                backText.setText("Login");
            } else {
                backText.setText("Back");
            }

            getCurrentStep().setProperty(this.registerTeacher);
            getCurrentStep().onUnmount();
            tabs.getSelectionModel().select(newIndex.intValue());
            getCurrentStep().onMount(this.registerTeacher);

            updateButtonListeners();
        });

        updateButtonListeners();
        canNext.addListener((obs, oldValue, nextAllowed) -> {
            if (nextAllowed) {
                nextButton.getStyleClass().remove("disabled");
            } else {
                nextButton.getStyleClass().add("disabled");
            }
        });
    }

    @Override
    public void onMount() {
        AppLogger.log("Teacher registration started");
        this.registerTeacher = new RegisterTeacher();
    }

    private void updateButtonListeners() {
        canNext.unbind();
        RegistrationStep<HasProperty> step = getCurrentStep();
        canNext.bind(step.validProperty());
    }


    public void next() {
        if (!canNext.get()) {
            return;
        }

        if (!stepper.isLast()) {
            AppLogger.logf("Registration step advanced: Step %d", stepper.getCurrentIndex() + 1);
            stepper.next();
        } else {
            AppLogger.logf("Registration form completed, attempting to register teacher: %s",
                this.registerTeacher.getEmail());
            boolean success = AuthenticationContext.registerTeacher(this.registerTeacher);
            if (success) {
                AppLogger.log("Registration successful, redirecting to dashboard");
                Router.changePage(PageName.PastRecords);
                Toast.show("User created!", Toast.ToastType.SUCCESS);
            } else {
                AppLogger.logf(LogLevel.Warning, "Registration failed for teacher: %s (ID may already exist)",
                    this.registerTeacher.getEmail());
                Toast.show("Error! Teacher ID already exists!", Toast.ToastType.ERROR);
            }
        }
    }

    public void prev() {
        if (stepper.getCurrentIndex() != 0) {
            stepper.previous();
        } else {
            Router.changePage(PageName.Login);
        }
    }

    private RegistrationStep<HasProperty> getCurrentStep() {
        return (RegistrationStep<HasProperty>) this.tabs.getSelectionModel().getSelectedItem();
    }
}
