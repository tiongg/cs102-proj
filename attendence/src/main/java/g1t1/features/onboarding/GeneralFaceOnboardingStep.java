package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import g1t1.utils.ImageUtils;
import javafx.beans.property.*;
import org.opencv.core.Rect;

// General face onboarding. Any face will do
public class GeneralFaceOnboardingStep implements FaceOnboardingStep {
    private final StringProperty instruction = new SimpleStringProperty("Scanning your face...");

    @Override
    public StringProperty instructionProperty() {
        // Doesnt actually change
        return instruction;
    }

    @Override
    public boolean isRegionValid(FaceInFrame faceRegion) {
        return true;
    }

    @Override
    public BooleanProperty isValid() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ObjectProperty<Rect> checkingRegion() {
        return new SimpleObjectProperty<>(ImageUtils.centerRect(160, 160, 256, 256));
    }
}
