package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import g1t1.utils.ImageUtils;
import javafx.beans.property.*;
import org.opencv.core.Rect;

// At the very start
public class StartFaceOnboardingStep implements FaceOnboardingStep {
    @Override
    public StringProperty instructionProperty() {
        return new SimpleStringProperty("Time to scan your face! Press the button to get started.");
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