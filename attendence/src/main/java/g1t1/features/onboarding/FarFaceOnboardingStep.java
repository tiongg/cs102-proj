package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import g1t1.utils.ImageUtils;
import javafx.beans.property.*;
import org.opencv.core.Rect;

// Far out pictures
public class FarFaceOnboardingStep implements FaceOnboardingStep {
    // Face should be maximum 40% the camera
    private static final double MIN_PROPORTION = (double) 2 / 5;
    private final BooleanProperty isFarEnough = new SimpleBooleanProperty();
    private final StringProperty instruction = new SimpleStringProperty();
    private final ObjectProperty<Rect> checkingRegionProperty = new SimpleObjectProperty<>(ImageUtils.centerRect(128, 128, 256, 256));

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
        int minSize = (int) Math.min(faceRegion.maxHeight() * MIN_PROPORTION, faceRegion.maxWidth() * MIN_PROPORTION);
        isFarEnough.set(faceRegion.faceBounds().width() < minSize && faceRegion.faceBounds().height() < minSize);
        return isFarEnough.get();
    }

    @Override
    public BooleanProperty isValid() {
        return isFarEnough;
    }

    @Override
    public ObjectProperty<Rect> checkingRegion() {
        return checkingRegionProperty;
    }
}
