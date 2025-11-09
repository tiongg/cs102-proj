package g1t1.opencv.services.preprocessing;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Bilateral filter processor for edge-preserving noise reduction.
 * Superior to Gaussian blur for face recognition:
 * - Smooths noise while preserving facial feature edges
 * - Maintains important structural information (eyes, nose, mouth)
 * - Better discrimination between similar faces
 */
public class BilateralFilterProcessor implements FacePreprocessor {

    @Override
    public Mat process(Mat face) {
        if (face == null || face.empty()) {
            return new Mat();
        }

        Mat filteredFace = new Mat();

        // Apply bilateral filter with optimized parameters for faces
        // d=5: neighborhood diameter (balanced for performance and quality)
        // sigmaColor=30: filter sigma in color space (controls color similarity)
        // sigmaSpace=30: filter sigma in coordinate space (controls spatial distance)
        // Lower sigma values preserve more facial detail while still reducing noise
        Imgproc.bilateralFilter(face, filteredFace, 5, 30, 30);

        return filteredFace;
    }
}
