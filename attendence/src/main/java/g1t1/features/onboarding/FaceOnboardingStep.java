package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import org.opencv.core.Rect;

public interface FaceOnboardingStep {
    public StringProperty instructionProperty();

    public BooleanProperty isValid();

    public ObjectProperty<Rect> checkingRegion();

    public boolean isRegionValid(FaceInFrame faceRegion);

}
