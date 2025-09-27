package g1t1.opencv.services.preprocessing;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Normalizes face image intensity for consistent lighting conditions.
 */
public class NormalizerProcessor implements FacePreprocessor {

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat normalizedFace = new Mat();
        face.copyTo(normalizedFace);

        Core.normalize(normalizedFace, normalizedFace, 0, 255, Core.NORM_MINMAX);

        return normalizedFace;
    }
}