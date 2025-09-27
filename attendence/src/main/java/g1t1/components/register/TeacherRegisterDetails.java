package g1t1.components.register;

import g1t1.models.ids.TeacherID;
import g1t1.models.interfaces.HasDetails;
import g1t1.models.scenes.Router;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public class TeacherRegisterDetails extends Tab implements RegistrationStep<HasDetails<TeacherID>> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);
    @FXML
    private TextField teacherIdInput;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField emailInput;

    public TeacherRegisterDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterDetails.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            reset();
        });

        teacherIdInput.textProperty().addListener(e -> updateIsValid());
        nameInput.textProperty().addListener(e -> updateIsValid());
        emailInput.textProperty().addListener(e -> updateIsValid());

        updateIsValid();
    }

    private void reset() {
        teacherIdInput.textProperty().set("");
        nameInput.textProperty().set("");
        emailInput.textProperty().set("");
    }

    private void updateIsValid() {
        validProperty().set(
                !teacherIdInput.textProperty().get().isEmpty() &&
                        !nameInput.textProperty().get().isEmpty() &&
                        !emailInput.textProperty().get().isEmpty()
        );
    }

    @Override
    public BooleanProperty validProperty() {
        return validProperty;
    }

    @Override
    public void setProperty(HasDetails<TeacherID> target) {
        target.setEmail(this.getEmail());
        target.setName(this.getName());
        target.setId(new TeacherID(getTeacherId()));
    }

    public String getTeacherId() {
        return this.teacherIdInput.getText();
    }

    public String getName() {
        return this.nameInput.getText();
    }

    public String getEmail() {
        return this.emailInput.getText();
    }
}
