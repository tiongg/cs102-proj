package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
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
            double correl = Imgproc.compareHist(faceHistogram, precomputedHistograms.get(0), Imgproc.HISTCMP_CORREL);
            double bhattacharyya = Imgproc.compareHist(faceHistogram, precomputedHistograms.get(0), Imgproc.HISTCMP_BHATTACHARYYA);
            double bhattacharyyaSim = Math.exp(-bhattacharyya);
            double quickCheck = (correl * 0.7) + (bhattacharyyaSim * 0.3);
            faceHistogram.release();

            if (quickCheck * 100.0 > 75.0) {
                return quickCheck * 100.0;
            }
        }

        // Full comparison against pre-computed histograms using multiple comparison methods
        Mat faceHistogram = calculateHistogram(processedFace);
        double bestSimilarity = 0.0;

        for (Mat precomputedHistogram : precomputedHistograms) {
            // Use correlation method (best for histogram comparison)
            double correl = Imgproc.compareHist(faceHistogram, precomputedHistogram, Imgproc.HISTCMP_CORREL);

            // Use Bhattacharyya distance (lower is better, convert to similarity)
            double bhattacharyya = Imgproc.compareHist(faceHistogram, precomputedHistogram, Imgproc.HISTCMP_BHATTACHARYYA);
            double bhattacharyyaSim = Math.exp(-bhattacharyya); // Convert distance to similarity

            // Optimized weighted combination emphasizing correlation
            // Only use correlation and Bhattacharyya (both normalized 0-1)
            double combinedSimilarity = (correl * 0.7) + (bhattacharyyaSim * 0.3);

            if (combinedSimilarity > bestSimilarity) {
                bestSimilarity = combinedSimilarity;
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

                // Use multiple comparison methods
                double correl = Imgproc.compareHist(faceHistogram, enrolledHistogram, Imgproc.HISTCMP_CORREL);
                double bhattacharyya = Imgproc.compareHist(faceHistogram, enrolledHistogram, Imgproc.HISTCMP_BHATTACHARYYA);
                double bhattacharyyaSim = Math.exp(-bhattacharyya);

                double combinedSimilarity = (correl * 0.7) + (bhattacharyyaSim * 0.3);

                if (combinedSimilarity > bestSimilarity) {
                    bestSimilarity = combinedSimilarity;
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
        // Create a multi-scale, multi-region histogram for better discrimination
        int height = image.rows();
        int width = image.cols();
        int halfHeight = height / 2;
        int halfWidth = width / 2;
        int thirdHeight = height / 3;
        int thirdWidth = width / 3;

        java.util.List<Mat> histograms = new java.util.ArrayList<>();

        // Full face histogram (global appearance)
        histograms.add(calculateSingleHistogram(image));

        // 4 quadrants (spatial structure)
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(0, 0, halfWidth, halfHeight))));
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(halfWidth, 0, width - halfWidth, halfHeight))));
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(0, halfHeight, halfWidth, height - halfHeight))));
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(halfWidth, halfHeight, width - halfWidth, height - halfHeight))));

        // 3x3 grid for finer details (eyes, nose, mouth regions)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x = j * thirdWidth;
                int y = i * thirdHeight;
                int w = (j == 2) ? (width - x) : thirdWidth;
                int h = (i == 2) ? (height - y) : thirdHeight;
                histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(x, y, w, h))));
            }
        }

        // Horizontal bands (emphasize eyes and mouth rows)
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(0, 0, width, thirdHeight)))); // Upper third (eyes)
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(0, thirdHeight, width, thirdHeight)))); // Middle third (nose)
        histograms.add(calculateSingleHistogram(new Mat(image, new org.opencv.core.Rect(0, 2 * thirdHeight, width, height - 2 * thirdHeight)))); // Lower third (mouth)

        // Combine all histograms into one feature vector
        Mat combined = new Mat();
        org.opencv.core.Core.vconcat(histograms, combined);

        // Cleanup
        for (Mat hist : histograms) {
            hist.release();
        }

        return combined;
    }

    private Mat calculateSingleHistogram(Mat image) {
        Mat histogram = new Mat();
        List<Mat> images = Arrays.asList(image);

        // Use more bins for better discrimination (64 bins instead of 256)
        // Reduces noise while preserving important details
        Imgproc.calcHist(
                images,
                new MatOfInt(0),
                new Mat(),
                histogram,
                new MatOfInt(64),
                new MatOfFloat(0f, 256f)
        );

        // Normalize using L2 norm for better comparison stability
        org.opencv.core.Core.normalize(histogram, histogram, 1, 0, org.opencv.core.Core.NORM_L2);

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