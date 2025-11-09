package g1t1.opencv.services;

import org.opencv.core.*;
import org.opencv.imgproc.*;
import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;

/**
 * Detects if a person is wearing a face mask by analyzing lower face region.
 */
public class MaskDetector {

    public boolean detectMask(Mat faceRegion) {
        if (faceRegion == null || faceRegion.empty()) {
            return false;
        }

        // Extract mouth/chin region (lower 50% of face)
        Mat lowerFace = extractLowerFaceRegion(faceRegion);

        // Method 1: Color uniformity in HSV
        Mat hsvFace = new Mat();
        if (lowerFace.channels() == 3) {
            Imgproc.cvtColor(lowerFace, hsvFace, Imgproc.COLOR_BGR2HSV);
        } else {
            Imgproc.cvtColor(lowerFace, hsvFace, Imgproc.COLOR_GRAY2BGR);
            Imgproc.cvtColor(hsvFace, hsvFace, Imgproc.COLOR_BGR2HSV);
        }

        double hueVariation = getColorUniformityScore(hsvFace);
        double saturationVariation = getSaturationUniformity(hsvFace);

        // Method 2: Edge analysis
        double edgeRatio = getEdgeScore(lowerFace);

        // Method 3: Texture analysis (skin has more texture than fabric)
        double textureScore = getTextureScore(lowerFace);

        lowerFace.release();
        hsvFace.release();

        // Multi-metric scoring - combination of indicators
        int maskScore = 0;

        // Edge pattern - masks have distinct edge from fabric boundary
        if (edgeRatio > 0.12 && edgeRatio < 0.24) maskScore += 2;

        // Color uniformity - masks are much more uniform than skin
        // Skin: Hue variance typically 25-50, Saturation variance 50-80
        // Mask: Hue variance typically 10-20, Saturation variance 20-40
        if (hueVariation < 20.0 && saturationVariation < 35.0) maskScore += 2;

        // Low texture - fabric masks are smoother than textured skin
        if (textureScore < 35.0) maskScore += 1;

        // Need at least 3 points from combination of metrics
        boolean hasMask = maskScore >= 3;

        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log(String.format("[Mask] Hue=%.1f Sat=%.1f Edge=%.3f Tex=%.1f Score=%d -> %s",
                hueVariation, saturationVariation, edgeRatio, textureScore, maskScore, hasMask ? "MASK" : "NO MASK"));
        }

        return hasMask;
    }

    private Mat extractLowerFaceRegion(Mat face) {
        // Extract lower 50% (mouth and chin area)
        int startY = (int) (face.height() * 0.5);
        int height = face.height() - startY;
        Rect lowerRegion = new Rect(0, startY, face.width(), height);
        return new Mat(face, lowerRegion);
    }

    private double getColorUniformityScore(Mat hsvFace) {
        Mat hueChannel = new Mat();
        Core.extractChannel(hsvFace, hueChannel, 0);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(hueChannel, mean, stddev);

        double hueVariation = stddev.get(0, 0)[0];
        hueChannel.release();

        return hueVariation;
    }

    private double getEdgeScore(Mat lowerFace) {
        Mat grayFace = new Mat();
        if (lowerFace.channels() == 3) {
            Imgproc.cvtColor(lowerFace, grayFace, Imgproc.COLOR_BGR2GRAY);
        } else {
            lowerFace.copyTo(grayFace);
        }

        Mat edges = new Mat();
        // Sensitive edge detection to catch mask boundaries
        Imgproc.Canny(grayFace, edges, 25, 100);

        int totalPixels = edges.rows() * edges.cols();
        int edgePixels = Core.countNonZero(edges);
        double edgeRatio = (double) edgePixels / totalPixels;

        grayFace.release();
        edges.release();

        return edgeRatio;
    }

    private double getSaturationUniformity(Mat hsvFace) {
        Mat satChannel = new Mat();
        Core.extractChannel(hsvFace, satChannel, 1); // Saturation channel

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(satChannel, mean, stddev);

        double satVariation = stddev.get(0, 0)[0];
        satChannel.release();

        return satVariation;
    }

    private double getTextureScore(Mat lowerFace) {
        Mat grayFace = new Mat();
        if (lowerFace.channels() == 3) {
            Imgproc.cvtColor(lowerFace, grayFace, Imgproc.COLOR_BGR2GRAY);
        } else {
            lowerFace.copyTo(grayFace);
        }

        // Calculate local standard deviation to measure texture
        Mat laplacian = new Mat();
        Imgproc.Laplacian(grayFace, laplacian, CvType.CV_64F);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, stddev);

        double textureVariance = stddev.get(0, 0)[0];

        grayFace.release();
        laplacian.release();

        return textureVariance;
    }
}