package g1t1.components.register;

import g1t1.models.interfaces.HasPassword;
import g1t1.models.scenes.Router;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

public class PasswordDetails extends Tab implements RegistrationStep<HasPassword> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);
    @FXML
    private TextField passwordInput;
    @FXML
    private TextField confirmPasswordInput;

    public PasswordDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PasswordDetails.fxml"));
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

        passwordInput.textProperty().addListener((e) -> updateValidProperty());
        confirmPasswordInput.textProperty().addListener((e) -> updateValidProperty());
    }

    private void reset() {
        passwordInput.textProperty().set("");
        confirmPasswordInput.textProperty().set("");
    }

    private void updateValidProperty() {
        validProperty().set(
                !passwordInput.textProperty().get().isEmpty() &&
                        passwordInput.textProperty().get().equals(confirmPasswordInput.textProperty().get())
        );
    }

    @Override
    public BooleanProperty validProperty() {
        return validProperty;
    }

    @Override
    public void setProperty(HasPassword target) {
        target.setPassword(this.getPassword());
    }

    public String getPassword() {
        return this.passwordInput.getText();
    }
}
