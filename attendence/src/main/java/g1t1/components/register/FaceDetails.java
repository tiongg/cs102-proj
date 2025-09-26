package g1t1.components.register;

import g1t1.models.interfaces.HasFaces;
import g1t1.models.scenes.Router;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

public class FaceDetails extends Tab implements RegistrationStep<HasFaces> {
    private final BooleanProperty isValid = new SimpleBooleanProperty(true);

    public FaceDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetails.fxml"));
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
        return this.isValid;
    }

    @Override
    public void setProperty(HasFaces value) {
        
    }
}
