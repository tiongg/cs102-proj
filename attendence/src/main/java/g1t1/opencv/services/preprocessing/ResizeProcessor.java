package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Resizes face images to standard dimensions for recognition.
 */
public class ResizeProcessor implements FacePreprocessor {

    private final Size targetSize;

    public ResizeProcessor(int width, int height) {
        this.targetSize = new Size(width, height);
    }

    public ResizeProcessor() {
        this(100, 100);
    }

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, targetSize);

        return resizedFace;
    }
}