package g1t1.components.register;

import g1t1.models.interfaces.HasFaces;
import g1t1.models.scenes.Router;
import g1t1.models.users.FaceData;
import g1t1.utils.events.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

public class FaceDetails extends Tab implements RegistrationStep<HasFaces> {
    private final BooleanProperty isValid = new SimpleBooleanProperty(true);
    private final IntegerProperty photosTaken = new SimpleIntegerProperty(0);
    private final List<FaceData> faceData = new ArrayList<>();
    private final int REQUIRED_PICTURE_COUNT = 15;

    private FaceData thumbnailImage;

    @FXML
    private Label lblTakenPictures;

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
            reset();
        });

        lblTakenPictures.textProperty().bind(photosTaken.map(x -> String.format("%d / %d", x.intValue(), REQUIRED_PICTURE_COUNT)));
        isValid.bind(photosTaken.map(x -> x.intValue() >= REQUIRED_PICTURE_COUNT));
    }

    @Override
    public BooleanProperty validProperty() {
        return this.isValid;
    }

    @Override
    public void setProperty(HasFaces target) {
        target.setFaceData(this.faceData);
    }

    private void reset() {
        this.photosTaken.set(0);
        this.faceData.clear();
        thumbnailImage = null;
    }

    public void takePicture() {
        this.photosTaken.set(this.photosTaken.get() + 1);

        // TODO: Take picture somehow

        thumbnailImage = null;
    }
}
