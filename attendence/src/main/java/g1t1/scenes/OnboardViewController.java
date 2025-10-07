package g1t1.scenes;

import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.PageController;
import g1t1.models.users.RegisterStudent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

public class OnboardViewController extends PageController {
    private final BooleanProperty canNext = new SimpleBooleanProperty(false);

    private RegisterStudent registerStudent;

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
                nextText.setText("Done!");
            } else {
                nextText.setText("Next");
            }

            backText.setText("Back");

            getCurrentStep().setProperty(this.registerStudent);
            getCurrentStep().onUnmount();
            tabs.getSelectionModel().select(newIndex.intValue());
            getCurrentStep().onMount(this.registerStudent);

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
        stepper.currentIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                backButton.getStyleClass().add("disabled");
            } else {
                backButton.getStyleClass().remove("disabled");
            }
        });
    }

    @Override
    public void onMount() {
        this.registerStudent = new RegisterStudent();
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
            stepper.next();
        } else {
            // Save to db
        }
    }

    public void prev() {
        if (stepper.getCurrentIndex() > 0) {
            stepper.previous();
        }
    }

    private RegistrationStep<HasProperty> getCurrentStep() {
        return (RegistrationStep<HasProperty>) this.tabs.getSelectionModel().getSelectedItem();
    }
}
