package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Histogram equalization processor for improved contrast and lighting normalization.
 * Essential for face recognition under varying lighting conditions.
 */
public class HistogramEqualizationProcessor implements FacePreprocessor {

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat equalizedFace = new Mat();

        // Apply histogram equalization to improve contrast
        Imgproc.equalizeHist(face, equalizedFace);

        return equalizedFace;
    }
}