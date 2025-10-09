package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Face recognizer that handles masked faces by focusing on upper face region.
 * Optimized with pre-computed histograms for better performance.
 */
public class MaskAwareRecognizer extends Recognizer {

    // Cache for pre-computed upper face histograms per student
    private final Map<String, List<Mat>> recognisedUpperHistograms = new ConcurrentHashMap<>();
    private final Map<String, Double> bestKnownSimilarities = new ConcurrentHashMap<>();

    /**
     * Pre-compute upper face histograms for all enrolled students.
     */
    public void precomputeEnrollmentData(List<? extends Recognisable> recognisableList) {
        long startTime = System.currentTimeMillis();
        recognisedUpperHistograms.clear();
        bestKnownSimilarities.clear();

        for (Recognisable recognisable : recognisableList) {
            if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
                continue;
            }

            List<Mat> histograms = new ArrayList<>();
            List<byte[]> faceImages = recognisable.getFaceData().getFaceImages();

            for (byte[] imageData : faceImages) {
                Mat enrolledFace = loadAndPreprocessEnrolledFace(imageData);
                if (!enrolledFace.empty()) {
                    Mat upperRegion = extractUpperFaceRegion(enrolledFace);
                    Mat histogram = calculateHistogram(upperRegion);
                    histograms.add(histogram);
                    enrolledFace.release();
                    upperRegion.release();
                }
            }

            if (!histograms.isEmpty()) {
                recognisedUpperHistograms.put(recognisable.getRecognitionId(), histograms);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Pre-computed mask-aware enrollment histograms for " + recognisableList.size() +
                    " recognisable objects in " + duration + "ms");
        }
    }

    @Override
    public double compareWithRecognisable(Mat processedFace, Recognisable recognisable) {
        String recognitionId = recognisable.getRecognitionId();
        List<Mat> precomputedHistograms = recognisedUpperHistograms.get(recognitionId);

        if (precomputedHistograms == null || precomputedHistograms.isEmpty()) {
            // Fallback to original method if no pre-computed data
            return fallbackCompareWithRecognisable(processedFace, recognisable);
        }

        Mat upperFaceRegion = extractUpperFaceRegion(processedFace);
        Mat faceHistogram = calculateHistogram(upperFaceRegion);
        double bestSimilarity = 0.0;

        for (Mat precomputedHistogram : precomputedHistograms) {
            double similarity = Imgproc.compareHist(faceHistogram, precomputedHistogram, Imgproc.HISTCMP_CORREL);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }

        faceHistogram.release();
        upperFaceRegion.release();

        double confidencePercentage = bestSimilarity * 100.0;
        bestKnownSimilarities.put(recognitionId, confidencePercentage);

        return confidencePercentage;
    }

    /**
     * Fallback method for when pre-computed data is not available
     */
    private double fallbackCompareWithRecognisable(Mat processedFace, Recognisable recognisable) {
        Mat upperFaceRegion = extractUpperFaceRegion(processedFace);
        Mat faceHistogram = calculateHistogram(upperFaceRegion);
        double bestSimilarity = 0.0;

        if (recognisable.getFaceData() != null && recognisable.getFaceData().getFaceImages() != null) {
            for (byte[] imageData : recognisable.getFaceData().getFaceImages()) {
                Mat enrolledFace = loadAndPreprocessEnrolledFace(imageData);
                Mat enrolledUpperRegion = extractUpperFaceRegion(enrolledFace);
                Mat enrolledHistogram = calculateHistogram(enrolledUpperRegion);

                double similarity = Imgproc.compareHist(faceHistogram, enrolledHistogram, Imgproc.HISTCMP_CORREL);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                }

                enrolledFace.release();
                enrolledUpperRegion.release();
                enrolledHistogram.release();
            }
        }

        faceHistogram.release();
        upperFaceRegion.release();
        return bestSimilarity * 100.0;
    }

    /**
     * Cleanup pre-computed data when service stops
     */
    public void cleanup() {
        for (List<Mat> histograms : recognisedUpperHistograms.values()) {
            for (Mat histogram : histograms) {
                histogram.release();
            }
        }
        recognisedUpperHistograms.clear();
        bestKnownSimilarities.clear();
    }

    private Mat extractUpperFaceRegion(Mat face) {
        int upperHeight = (int) (face.height() * 0.6);
        Rect upperRegion = new Rect(0, 0, face.width(), upperHeight);
        return new Mat(face, upperRegion);
    }

    private Mat calculateHistogram(Mat image) {
        Mat histogram = new Mat();
        List<Mat> images = Arrays.asList(image);

        Imgproc.calcHist(
                images,
                new MatOfInt(0),
                new Mat(),
                histogram,
                new MatOfInt(256),
                new MatOfFloat(0, 256)
        );

        return histogram;
    }

    private Mat loadAndPreprocessEnrolledFace(byte[] imageData) {
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            return new Mat();
        }
        return preprocessFace(image);
    }
}