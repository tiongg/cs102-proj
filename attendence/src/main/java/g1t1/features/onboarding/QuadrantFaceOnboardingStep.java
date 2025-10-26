package g1t1.features.onboarding;

import g1t1.opencv.models.FaceInFrame;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class QuadrantFaceOnboardingStep implements FaceOnboardingStep {
    private final Quadrant targetQuad;
    private final StringProperty instruction;

    public QuadrantFaceOnboardingStep(Quadrant targetQuad) {
        this.targetQuad = targetQuad;
        this.instruction = new SimpleStringProperty(String.format("Now in the %s quadrant!", this.targetQuad.label));
    }

    @Override
    public StringProperty instructionProperty() {
        return this.instruction;
    }

    @Override
    public boolean isRegionValid(FaceInFrame faceRegion) {
        int centerX = faceRegion.x() + (faceRegion.faceBounds().width() / 2);
        int centerY = faceRegion.y() + (faceRegion.faceBounds().height() / 2);
        int isRight = centerX > (faceRegion.maxWidth() / 2) ? 1 : 0;
        int isBottom = centerY > (faceRegion.maxHeight() / 2) ? 1 : 0;
        int mask = (isBottom << 1) | (isRight << 0);

        return this.targetQuad.mask == mask;
    }

    public enum Quadrant {
        // Use bit positions to determine if it needs to be matched
        // Leftmost bit =>  Top = 0, Bottom = 1
        // Rightmost bit => Right = 1, Left = 0
        TOP_LEFT(0, "top left"),           //0b00
        TOP_RIGHT(1, "top right"),         //0b01
        BOTTOM_LEFT(2, "bottom left"),     //0b10
        BOTTOM_RIGHT(3, "bottom right");   //0b11

        public final int mask;
        public final String label;

        Quadrant(int mask, String label) {
            this.mask = mask;
            this.label = label;
        }
    }
}
