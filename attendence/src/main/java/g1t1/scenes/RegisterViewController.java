package g1t1.scenes;

import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class RegisterViewController extends PageController {
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

    private boolean canNext = false;
    private final ChangeListener<Boolean> validationChangeListener = (observableValue, oldValue, newValue) -> {
        canNext = newValue;
    };
    private RegistrationStep prevStep;

    @Override
    public void onMount() {
        stepper.currentIndexProperty().addListener((obv, oldNumber, newNumber) -> {
            if (stepper.isLast()) {
                nextText.setText("Login!");
            } else {
                nextText.setText("Next");
            }

            if (newNumber.intValue() == 0) {
                backText.setText("Login");
            } else {
                backText.setText("Back");
            }

            updateButtonListeners();
        });

        updateButtonListeners();
    }

    private void updateButtonListeners() {
        if (prevStep != null) {
            prevStep.validProperty().removeListener(validationChangeListener);
        }
        RegistrationStep step = (RegistrationStep) tabs.getSelectionModel().getSelectedItem();
        prevStep = step;
        step.validProperty().addListener(validationChangeListener);
    }


    public void next() {
        if (!canNext) {
            return;
        }

        if (!stepper.isLast()) {
            stepper.next();
            int newIndex = stepper.getCurrentIndex();
            tabs.getSelectionModel().select(newIndex);
        } else {
            // TODO: Auth logic
            Router.changePage(PageName.PastRecords);
        }
    }

    public void prev() {
        if (stepper.getCurrentIndex() != 0) {
            stepper.previous();
            tabs.getSelectionModel().select(stepper.getCurrentIndex());
        } else {
            Router.changePage(PageName.Login);
        }
    }
}
