package g1t1.components.register;

import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.Router;
import g1t1.models.users.RegisterTeacher;
import g1t1.utils.ImageUtils;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

public class TeacherRegistrationDone extends Tab implements RegistrationStep<HasProperty> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);
    @FXML
    private Label lblName;
    @FXML
    private Label lblId;
    @FXML
    private Label lblEmail;
    @FXML
    private ImageView ivThumbnail;

    public TeacherRegistrationDone() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TeacherRegistrationDone.fxml"));
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
    }

    private void reset() {

    }

    @Override
    public BooleanProperty validProperty() {
        return this.validProperty;
    }

    @Override
    public void onMount(Object currentRegistrant) {
        RegisterTeacher registrationDetails = (RegisterTeacher) currentRegistrant;
        lblName.setText(registrationDetails.getFullName());
        lblId.setText(registrationDetails.getTeacherID().toString());
        lblEmail.setText(registrationDetails.getEmail());
        ivThumbnail.setImage(ImageUtils.bytesToImage(registrationDetails.getThumbnailFace()));
    }
}
