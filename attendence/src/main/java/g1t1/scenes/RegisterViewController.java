package g1t1.scenes;

import g1t1.components.stepper.StepperControl;
import g1t1.models.scenes.PageController;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class RegisterViewController extends PageController {
    @FXML
    StepperControl stepper;
    @FXML
    TabPane tabs;

    public void next() {
        stepper.next();
        tabs.getSelectionModel().select(stepper.getCurrentIndex());
    }

    public void prev() {
        stepper.previous();
        tabs.getSelectionModel().select(stepper.getCurrentIndex());
    }
}
