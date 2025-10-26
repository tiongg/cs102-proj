package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Far out pictures
public class FarFaceOnboardingStep implements FaceOnboardingStep {
    private static final int MIN_SIZE = 100;
    private final BooleanProperty isFarEnough = new SimpleBooleanProperty();
    private final StringProperty instruction = new SimpleStringProperty();

    public FarFaceOnboardingStep() {
        instruction.bind(isFarEnough.map(x -> {
            if (!x) {
                return "Back it up now...";
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
        isFarEnough.set(faceRegion.faceBounds().width() < MIN_SIZE && faceRegion.faceBounds().height() < MIN_SIZE);
        return isFarEnough.get();
    }
}
