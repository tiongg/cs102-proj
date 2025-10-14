package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mask-aware face recognition using histogram, LBP, and HOG features on upper face region.
 * Focuses on upper 60% of face (eyes, forehead) for masked individuals.
 *
 * Preprocessing pipeline: Grayscale → Bilateral Filter → Normalize → CLAHE → Resize → Upper Region
 */
public class MaskAwareRecognizer extends Recognizer {

    private final Map<String, List<FaceFeatures>> studentFeatures = new ConcurrentHashMap<>();

    /**
     * Pre-computes features for upper face region of all enrolled students.
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
                    Mat upperRegion = extractUpperFaceRegion(processedFace);
                    features.add(extractFeatures(upperRegion));
                    upperRegion.release();
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
            AppLogger.log("Pre-computed mask-aware features for " + recognisableList.size() +
                    " students in " + duration + "ms");
        }
    }

    /**
     * Compares preprocessed face against enrolled student using upper face region.
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

        Mat upperRegion = extractUpperFaceRegion(processedFace);
        FaceFeatures detectedFeatures = extractFeatures(upperRegion);
        double bestSimilarity = 0.0;

        for (FaceFeatures enrolledFeatures : precomputedFeatures) {
            double similarity = compareFeatures(detectedFeatures, enrolledFeatures);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
            }
        }

        detectedFeatures.release();
        upperRegion.release();
        return bestSimilarity * 100.0;
    }

    /**
     * Fallback comparison when pre-computed data unavailable.
     */
    private double fallbackCompare(Mat processedFace, Recognisable recognisable) {
        if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
            return 0.0;
        }

        Mat upperRegion = extractUpperFaceRegion(processedFace);
        FaceFeatures detectedFeatures = extractFeatures(upperRegion);
        double bestSimilarity = 0.0;

        for (byte[] imageData : recognisable.getFaceData().getFaceImages()) {
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
            if (!image.empty()) {
                Mat enrolledProcessed = preprocessFace(image);
                Mat enrolledUpper = extractUpperFaceRegion(enrolledProcessed);
                FaceFeatures enrolledFeatures = extractFeatures(enrolledUpper);
                double similarity = compareFeatures(detectedFeatures, enrolledFeatures);

                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                }

                enrolledFeatures.release();
                enrolledUpper.release();
                enrolledProcessed.release();
                image.release();
            }
        }

        detectedFeatures.release();
        upperRegion.release();
        return bestSimilarity * 100.0;
    }

    /**
     * Extracts upper 60% of face region (eyes, forehead area).
     */
    private Mat extractUpperFaceRegion(Mat face) {
        int upperHeight = (int) (face.height() * 0.6);
        Rect upperRegion = new Rect(0, 0, face.width(), upperHeight);
        return new Mat(face, upperRegion);
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
