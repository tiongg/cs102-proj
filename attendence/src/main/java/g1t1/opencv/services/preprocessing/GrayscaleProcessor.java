package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Converts face images to grayscale for consistent processing.
 */
public class GrayscaleProcessor implements FacePreprocessor {

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat grayFace = new Mat();
        if (face.channels() == 3) {
            Imgproc.cvtColor(face, grayFace, Imgproc.COLOR_BGR2GRAY);
        } else {
            face.copyTo(grayFace);
        }

        return grayFace;
    }
}