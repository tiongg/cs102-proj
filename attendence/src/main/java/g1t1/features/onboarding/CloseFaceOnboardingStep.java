package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Close up pictures
public class CloseFaceOnboardingStep implements FaceOnboardingStep {
    private static final int MAX_SIZE = 150;
    private final BooleanProperty isCloseEnough = new SimpleBooleanProperty();
    private final StringProperty instruction = new SimpleStringProperty();

    public CloseFaceOnboardingStep() {
        instruction.bind(isCloseEnough.map(x -> {
            if (!x) {
                return "Close ups! Move closer to the camera";
            }
            return "Perfect! Stay at that distance";
        }));
    }

    @Override
    public StringProperty instructionProperty() {
        return instruction;
    }

    @Override
    public boolean isRegionValid(FaceInFrame faceRegion) {
        isCloseEnough.set(faceRegion.faceBounds().width() > MAX_SIZE && faceRegion.faceBounds().height() > MAX_SIZE);
        return isCloseEnough.get();
    }
}
