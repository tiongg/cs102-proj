package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import g1t1.utils.ImageUtils;
import javafx.beans.property.*;
import org.opencv.core.Rect;

// Close up pictures
public class CloseFaceOnboardingStep implements FaceOnboardingStep {
    // Face should be at least 1 / 2 the camera
    private static final double MAX_PROPORTION = (double) 1 / 2;
    private final BooleanProperty isCloseEnough = new SimpleBooleanProperty();
    private final StringProperty instruction = new SimpleStringProperty();
    private final ObjectProperty<Rect> checkingRegionProperty = new SimpleObjectProperty<>(ImageUtils.centerRect(192, 192, 256, 256));

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
        int maxSize = (int) Math.min(faceRegion.maxHeight() * MAX_PROPORTION, faceRegion.maxWidth() * MAX_PROPORTION);
        isCloseEnough.set(faceRegion.faceBounds().width() > maxSize && faceRegion.faceBounds().height() > maxSize);
        return isCloseEnough.get();
    }

    @Override
    public BooleanProperty isValid() {
        return isCloseEnough;
    }

    @Override
    public ObjectProperty<Rect> checkingRegion() {
        return checkingRegionProperty;
    }
}
