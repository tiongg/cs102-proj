package g1t1.scenes;

import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class RegisterViewController extends PageController {
    private final BooleanProperty canNext = new SimpleBooleanProperty(false);

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

    @Override
    public void onMount() {
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
            tabs.getSelectionModel().select(newIndex.intValue());
            updateButtonListeners();
        });

        updateButtonListeners();
        canNext.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                nextButton.getStyleClass().remove("disabled");
            } else {
                nextButton.getStyleClass().add("disabled");
            }
        });
    }

    private void updateButtonListeners() {
        canNext.unbind();
        RegistrationStep step = (RegistrationStep) tabs.getSelectionModel().getSelectedItem();
        canNext.bind(step.validProperty());
    }


    public void next() {
        if (!canNext.get()) {
            return;
        }

        if (!stepper.isLast()) {
            stepper.next();
        } else {
            // TODO: Auth logic
            Router.changePage(PageName.PastRecords);
        }
    }

    public void prev() {
        if (stepper.getCurrentIndex() != 0) {
            stepper.previous();
        } else {
            Router.changePage(PageName.Login);
        }
    }
}
