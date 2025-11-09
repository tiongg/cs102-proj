package g1t1.features.onboarding;

import javafx.beans.property.*;
import org.opencv.core.Rect;

record PictureRequirement(FaceOnboardingStep step, int numberOfPictures) {
}

public class FaceStepTracker {
    // Ensure this adds up to 100!!
    private static final PictureRequirement[] PICTURE_REQUIREMENTS = new PictureRequirement[]{
            new PictureRequirement(new StartFaceOnboardingStep(), 0),
            new PictureRequirement(new GeneralFaceOnboardingStep(), 20),
            new PictureRequirement(new CloseFaceOnboardingStep(), 20),
            new PictureRequirement(new FarFaceOnboardingStep(), 20),
            new PictureRequirement(new QuadrantFaceOnboardingStep(QuadrantFaceOnboardingStep.Quadrant.TOP_LEFT), 10),
            new PictureRequirement(new QuadrantFaceOnboardingStep(QuadrantFaceOnboardingStep.Quadrant.TOP_RIGHT), 10),
            new PictureRequirement(new QuadrantFaceOnboardingStep(QuadrantFaceOnboardingStep.Quadrant.BOTTOM_RIGHT), 10),
            new PictureRequirement(new QuadrantFaceOnboardingStep(QuadrantFaceOnboardingStep.Quadrant.BOTTOM_LEFT), 10),
    };

    public final ObjectProperty<FaceOnboardingStep> currentStep = new SimpleObjectProperty<>();
    public final ObjectProperty<Rect> currentRegion = new SimpleObjectProperty<>();
    private final StringProperty instruction = new SimpleStringProperty();
    private final IntegerProperty faceStepIndex = new SimpleIntegerProperty(0);
    private final ObjectProperty<PictureRequirement> currentRequirement = new SimpleObjectProperty<>();
    private int picturesInStep = 0;

    public FaceStepTracker(ListProperty<byte[]> photosTaken) {
        currentRequirement.bind(faceStepIndex.map(i -> PICTURE_REQUIREMENTS[i.intValue() % PICTURE_REQUIREMENTS.length]));
        currentStep.bind(currentRequirement.map(PictureRequirement::step));
        instruction.bind(currentStep.flatMap(FaceOnboardingStep::instructionProperty));

        currentStep.subscribe(step -> {
            currentRegion.unbind();
            currentRegion.bind(step.checkingRegion());
        });

        photosTaken.subscribe((e) -> {
            if (picturesInStep < currentRequirement.get().numberOfPictures()) {
                picturesInStep++;
            } else {
                picturesInStep = 1;
                faceStepIndex.set((faceStepIndex.intValue() + 1) % PICTURE_REQUIREMENTS.length);
            }
        });
        reset();
    }

    public StringProperty instructionProperty() {
        return instruction;
    }

    public void reset() {
        this.faceStepIndex.set(0);
        this.picturesInStep = 0;
    }
}
