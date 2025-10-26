package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.StringProperty;

public interface FaceOnboardingStep {
    public StringProperty instructionProperty();

    public boolean isRegionValid(FaceInFrame faceRegion);
}
