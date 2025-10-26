package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
}