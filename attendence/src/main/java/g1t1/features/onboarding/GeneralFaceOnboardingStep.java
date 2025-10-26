package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
}
