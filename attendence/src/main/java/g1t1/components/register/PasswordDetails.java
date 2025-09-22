package g1t1.components.register;

import g1t1.models.scenes.Router;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

public class PasswordDetails extends Tab implements RegistrationStep {
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
        });
    }

    @Override
    public BooleanProperty validProperty() {
        return new SimpleBooleanProperty();
    }
}
