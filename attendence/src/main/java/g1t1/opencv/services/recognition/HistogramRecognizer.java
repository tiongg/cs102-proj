package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized face recognition using pre-computed histograms.
 * Dramatically improves performance by caching enrollment data.
 */
public class HistogramRecognizer extends Recognizer {

    // Cache for pre-computed histograms per student
    private final Map<String, List<Mat>> studentHistograms = new ConcurrentHashMap<>();
    private final Map<String, Double> bestKnownSimilarities = new ConcurrentHashMap<>();

    /**
     * Pre-compute histograms for all enrolled students.
     * Call this once when service starts instead of processing every frame.
     */
    public void precomputeEnrollmentData(List<? extends Recognisable> recognisableList) {
        long startTime = System.currentTimeMillis();
        studentHistograms.clear();
        bestKnownSimilarities.clear();

        for (Recognisable student : recognisableList) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            List<Mat> histograms = new ArrayList<>();
            List<byte[]> faceImages = student.getFaceData().getFaceImages();

            for (byte[] imageData : faceImages) {
                Mat enrolledFace = loadAndPreprocessEnrolledFace(imageData);
                if (!enrolledFace.empty()) {
                    Mat histogram = calculateHistogram(enrolledFace);
                    histograms.add(histogram);
                    enrolledFace.release();
                }
            }

            if (!histograms.isEmpty()) {
                studentHistograms.put(student.getRecognitionId(), histograms);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Pre-computed enrollment histograms for " + recognisableList.size() +
                    " students in " + duration + "ms");
        }
    }

    @Override
    public double compareWithRecognisable(Mat processedFace, Recognisable recognisable) {
        String recognitionId = recognisable.getRecognitionId();
        List<Mat> precomputedHistograms = studentHistograms.get(recognitionId);

        if (precomputedHistograms == null || precomputedHistograms.isEmpty()) {
            // Fallback to original method if no pre-computed data
            return fallbackCompareWithRecognisable(processedFace, recognisable);
        }

        // Quick cache check - if we've seen this student recently with high confidence
        Double recentSimilarity = bestKnownSimilarities.get(recognitionId);
        if (recentSimilarity != null && recentSimilarity > 80.0) {
            // Fast-track high-confidence students (performance optimization)
            Mat faceHistogram = calculateHistogram(processedFace);
            double quickCheck = Imgproc.compareHist(faceHistogram, precomputedHistograms.get(0), Imgproc.HISTCMP_CORREL);
            faceHistogram.release();

            if (quickCheck * 100.0 > 70.0) {
                return quickCheck * 100.0;
            }
        }

        // Full comparison against pre-computed histograms
        Mat faceHistogram = calculateHistogram(processedFace);
        double bestSimilarity = 0.0;

        for (Mat precomputedHistogram : precomputedHistograms) {
            double similarity = Imgproc.compareHist(faceHistogram, precomputedHistogram, Imgproc.HISTCMP_CORREL);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }

        faceHistogram.release();

        // Cache the result for fast-tracking
        double confidencePercentage = bestSimilarity * 100.0;
        bestKnownSimilarities.put(recognitionId, confidencePercentage);

        return confidencePercentage;
    }

    /**
     * Fallback method for when pre-computed data is not available
     */
    private double fallbackCompareWithRecognisable(Mat processedFace, Recognisable recognisable) {
        if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
            return 0.0;
        }

        Mat faceHistogram = calculateHistogram(processedFace);
        double bestSimilarity = 0.0;

        List<byte[]> faceImages = recognisable.getFaceData().getFaceImages();
        for (byte[] imageData : faceImages) {
            Mat enrolledFace = loadAndPreprocessEnrolledFace(imageData);
            if (!enrolledFace.empty()) {
                Mat enrolledHistogram = calculateHistogram(enrolledFace);
                double similarity = Imgproc.compareHist(faceHistogram, enrolledHistogram, Imgproc.HISTCMP_CORREL);

                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                }

                enrolledHistogram.release();
            }
            enrolledFace.release();
        }

        faceHistogram.release();
        return bestSimilarity * 100.0;
    }

    private Mat loadAndPreprocessEnrolledFace(byte[] imageData) {
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            return new Mat();
        }

        Mat processed = preprocessFace(image);
        image.release();
        return processed;
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
                new MatOfFloat(0f, 256f)
        );

        return histogram;
    }

    /**
     * Cleanup pre-computed data when service stops
     */
    public void cleanup() {
        for (List<Mat> histograms : studentHistograms.values()) {
            for (Mat histogram : histograms) {
                histogram.release();
            }
        }
        studentHistograms.clear();
        bestKnownSimilarities.clear();
    }
}