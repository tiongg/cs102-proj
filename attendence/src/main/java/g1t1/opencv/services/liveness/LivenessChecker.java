package g1t1.opencv.services.liveness;

import g1t1.opencv.models.*;
import org.opencv.core.*;
import org.opencv.imgproc.*;

/**
 * Basic liveness detection to prevent photo spoofing.
 */
public class LivenessChecker {

    /**
     * Simple liveness check based on face texture analysis.
     * Real faces have more texture variation than photos.
     */
    public LivenessResult checkLiveness(Mat faceRegion) {
        if (faceRegion == null || faceRegion.empty()) {
            return new LivenessResult(false, 0.0, "No face region provided");
        }

        // Convert to grayscale for texture analysis
        Mat grayFace = new Mat();
        if (faceRegion.channels() == 3) {
            Imgproc.cvtColor(faceRegion, grayFace, Imgproc.COLOR_BGR2GRAY);
        } else {
            faceRegion.copyTo(grayFace);
        }

        // Calculate texture variation using Laplacian
        Mat laplacian = new Mat();
        Imgproc.Laplacian(grayFace, laplacian, CvType.CV_64F);

        // Calculate variance of Laplacian
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);

        double textureVariance = stddev.get(0, 0)[0] * stddev.get(0, 0)[0];

        // Cleanup
        grayFace.release();
        laplacian.release();

        // Simple threshold: real faces typically have higher texture variance
        double threshold = 100.0; // Adjust based on testing
        boolean isLive = textureVariance > threshold;
        double confidence = Math.min(textureVariance / threshold, 1.0) * 100.0;

        String reason = isLive ? "Sufficient texture variation detected" : "Low texture variation (possible photo)";

        return new LivenessResult(isLive, confidence, reason);
    }
}