package g1t1.opencv.services.liveness;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.LivenessResult;

/**
 * Basic liveness detection to prevent photo spoofing.
 */
public class LivenessChecker {

    /**
     * Multi-metric liveness check combining texture and edge analysis.
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

        // Method 1: Laplacian variance (actual variance, not stddev)
        Mat laplacian = new Mat();
        Imgproc.Laplacian(grayFace, laplacian, CvType.CV_64F);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);

        double laplacianStdDev = stddev.get(0, 0)[0];
        double laplacianVariance = laplacianStdDev * laplacianStdDev;

        // Method 2: Texture complexity ratio (high-frequency / low-frequency)
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(grayFace, blurred, new Size(9, 9), 0);

        Mat highFreq = new Mat();
        Core.absdiff(grayFace, blurred, highFreq);

        Scalar highFreqMean = Core.mean(highFreq);
        Scalar lowFreqMean = Core.mean(blurred);

        double textureRatio = highFreqMean.val[0] / (lowFreqMean.val[0] + 1.0);

        // Cleanup
        grayFace.release();
        laplacian.release();
        blurred.release();
        highFreq.release();

        // Combined scoring - calibrated thresholds
        // Photos: low variance (< 250), low texture ratio (< 0.06)
        // Real faces: high variance (> 350), high texture ratio (> 0.08)

        double varianceScore = laplacianVariance > 320.0 ? 1.0 : 0.0;
        double textureScore = textureRatio > 0.08 ? 1.0 : 0.0;

        // BOTH metrics MUST pass for live detection
        boolean isLive = (varianceScore + textureScore) >= 2.0;
        double confidence = ((varianceScore + textureScore) / 2.0) * 100.0;

        String reason = String.format("Var=%.0f Ratio=%.3f -> %s",
            laplacianVariance, textureRatio, isLive ? "LIVE" : "PHOTO");

        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log(String.format("[Liveness] %s", reason));
        }

        return new LivenessResult(isLive, confidence, reason);
    }
}