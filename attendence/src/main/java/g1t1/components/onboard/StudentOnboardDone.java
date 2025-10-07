package g1t1.components.onboard;

import g1t1.components.register.RegistrationStep;
import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.RegisterStudent;
import g1t1.utils.ImageUtils;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

public class StudentOnboardDone extends Tab implements RegistrationStep<HasProperty> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(true);

    @FXML
    private Label lblName;
    @FXML
    private Label lblId;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblModuleSection;
    @FXML
    private ImageView ivThumbnail;

    public StudentOnboardDone() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentRegistrationDone.fxml"));
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

    @Override
    public void reset() {
        lblName.setText("");
        lblId.setText("");
        lblEmail.setText("");
        ivThumbnail.setImage(null);
    }


    @Override
    public BooleanProperty validProperty() {
        return this.validProperty;
    }

    @Override
    public void onMount(Object currentRegistrant) {
        RegisterStudent registrationDetails = (RegisterStudent) currentRegistrant;
        lblName.setText(registrationDetails.getName());
        lblId.setText(registrationDetails.getStudentID().toString());
        lblEmail.setText(registrationDetails.getEmail());
        ivThumbnail.setImage(ImageUtils.bytesToImage(registrationDetails.getThumbnail()));

        ModuleSection moduleSection = registrationDetails.getModuleSection();
        lblModuleSection.setText(String.format("%s - %s", moduleSection.getModule(), moduleSection.getSection()));
    }
}
