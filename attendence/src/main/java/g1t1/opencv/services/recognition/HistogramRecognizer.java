package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Full-face recognition using histogram, LBP, and HOG features.
 * Analyzes entire face for identification when no mask is present.
 *
 * Preprocessing pipeline: Grayscale → Bilateral Filter → Normalize → CLAHE → Resize
 */
public class HistogramRecognizer extends Recognizer {

    private final Map<String, List<FaceFeatures>> studentFeatures = new ConcurrentHashMap<>();

    /**
     * Pre-computes features for all enrolled students.
     */
    public void precomputeEnrollmentData(List<? extends Recognisable> recognisableList) {
        long startTime = System.currentTimeMillis();
        studentFeatures.clear();

        for (Recognisable student : recognisableList) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            List<FaceFeatures> features = new ArrayList<>();
            for (byte[] imageData : student.getFaceData().getFaceImages()) {
                Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
                if (!image.empty()) {
                    Mat processedFace = preprocessFace(image);
                    features.add(extractFeatures(processedFace));
                    processedFace.release();
                    image.release();
                }
            }

            if (!features.isEmpty()) {
                studentFeatures.put(student.getRecognitionId(), features);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Pre-computed features for " + recognisableList.size() +
                    " students in " + duration + "ms");
        }
    }

    /**
     * Compares preprocessed face against enrolled student.
     *
     * @param processedFace Preprocessed face image
     * @param recognisable Student to compare against
     * @return Confidence percentage [0-100]
     */
    @Override
    public double compareWithRecognisable(Mat processedFace, Recognisable recognisable) {
        String recognitionId = recognisable.getRecognitionId();
        List<FaceFeatures> precomputedFeatures = studentFeatures.get(recognitionId);

        if (precomputedFeatures == null || precomputedFeatures.isEmpty()) {
            return fallbackCompare(processedFace, recognisable);
        }

        FaceFeatures detectedFeatures = extractFeatures(processedFace);
        double bestSimilarity = 0.0;

        for (FaceFeatures enrolledFeatures : precomputedFeatures) {
            double similarity = compareFeatures(detectedFeatures, enrolledFeatures);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }

        detectedFeatures.release();
        return bestSimilarity * 100.0;
    }

    /**
     * Fallback comparison when pre-computed data unavailable.
     */
    private double fallbackCompare(Mat processedFace, Recognisable recognisable) {
        if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
            return 0.0;
        }

        FaceFeatures detectedFeatures = extractFeatures(processedFace);
        double bestSimilarity = 0.0;

        for (byte[] imageData : recognisable.getFaceData().getFaceImages()) {
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
            if (!image.empty()) {
                Mat enrolledProcessed = preprocessFace(image);
                FaceFeatures enrolledFeatures = extractFeatures(enrolledProcessed);
                double similarity = compareFeatures(detectedFeatures, enrolledFeatures);

                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                }

                enrolledFeatures.release();
                enrolledProcessed.release();
                image.release();
            }
        }

        detectedFeatures.release();
        return bestSimilarity * 100.0;
    }

    /**
     * Releases all pre-computed data.
     */
    public void cleanup() {
        for (List<FaceFeatures> features : studentFeatures.values()) {
            for (FaceFeatures feature : features) {
                feature.release();
            }
        }
        studentFeatures.clear();
    }
}
