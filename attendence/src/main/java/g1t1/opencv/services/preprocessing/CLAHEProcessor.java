package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

/**
 * CLAHE (Contrast Limited Adaptive Histogram Equalization) processor.
 * Superior to basic histogram equalization for face recognition:
 * - Adapts to local contrast variations
 * - Prevents over-amplification of noise
 * - Better handles varying lighting conditions
 */
public class CLAHEProcessor implements FacePreprocessor {

    private final CLAHE clahe;

    public CLAHEProcessor() {
        // Create CLAHE with optimized parameters for face recognition
        this.clahe = Imgproc.createCLAHE();

        // Clip limit: controls contrast amplification (2.0-4.0 works well for faces)
        this.clahe.setClipLimit(3.0);

        // Tile grid size: 8x8 provides good balance between local and global adaptation
        this.clahe.setTilesGridSize(new Size(8, 8));
    }

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat enhancedFace = new Mat();

        // Apply CLAHE to improve contrast while limiting noise amplification
        clahe.apply(face, enhancedFace);

        return enhancedFace;
    }
}
