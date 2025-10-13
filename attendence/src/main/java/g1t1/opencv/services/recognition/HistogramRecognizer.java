package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Face recognition using histogram, LBP, and HOG features.
 * Combines three complementary feature types for high identification accuracy
 * with strong discrimination between different individuals.
 *
 * Preprocessing pipeline (from Recognizer base class):
 * Grayscale → Bilateral Filter → Normalize → CLAHE → Resize
 */
public class HistogramRecognizer extends Recognizer {

    private final Map<String, List<FaceFeatures>> studentFeatures = new ConcurrentHashMap<>();
    private final HOGDescriptor hogDescriptor;

    /**
     * Container for face feature data.
     */
    private static class FaceFeatures {
        final Mat histogram;
        final Mat lbp;
        final Mat hog;

        FaceFeatures(Mat histogram, Mat lbp, Mat hog) {
            this.histogram = histogram;
            this.lbp = lbp;
            this.hog = hog;
        }

        void release() {
            histogram.release();
            lbp.release();
            hog.release();
        }
    }

    public HistogramRecognizer() {
        super();
        this.hogDescriptor = new HOGDescriptor(
            new Size(64, 128),
            new Size(16, 16),
            new Size(8, 8),
            new Size(8, 8),
            9
        );
    }

    /**
     * Pre-computes features for all enrolled students.
     * Uses full preprocessing pipeline from base Recognizer class.
     *
     * @param recognisableList List of enrolled students
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
     * Face is already preprocessed by Recognizer base class.
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
     * Extracts all three feature types from preprocessed face.
     */
    private FaceFeatures extractFeatures(Mat face) {
        return new FaceFeatures(
            calculateHistogram(face),
            calculateLBP(face),
            calculateHOG(face)
        );
    }

    /**
     * Calculates intensity histogram (128 bins).
     */
    private Mat calculateHistogram(Mat image) {
        Mat histogram = new Mat();
        Imgproc.calcHist(
            Arrays.asList(image),
            new MatOfInt(0),
            new Mat(),
            histogram,
            new MatOfInt(128),
            new MatOfFloat(0f, 256f)
        );
        Core.normalize(histogram, histogram, 1, 0, Core.NORM_L2);
        return histogram;
    }

    /**
     * Calculates Local Binary Pattern histogram for texture features.
     */
    private Mat calculateLBP(Mat image) {
        int height = image.rows();
        int width = image.cols();
        if (height < 3 || width < 3) {
            return new Mat();
        }

        Mat lbpImage = new Mat(height - 2, width - 2, CvType.CV_8UC1);

        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                double center = image.get(i, j)[0];
                int lbpValue = 0;

                lbpValue |= (image.get(i - 1, j - 1)[0] >= center ? 1 : 0) << 7;
                lbpValue |= (image.get(i - 1, j)[0] >= center ? 1 : 0) << 6;
                lbpValue |= (image.get(i - 1, j + 1)[0] >= center ? 1 : 0) << 5;
                lbpValue |= (image.get(i, j + 1)[0] >= center ? 1 : 0) << 4;
                lbpValue |= (image.get(i + 1, j + 1)[0] >= center ? 1 : 0) << 3;
                lbpValue |= (image.get(i + 1, j)[0] >= center ? 1 : 0) << 2;
                lbpValue |= (image.get(i + 1, j - 1)[0] >= center ? 1 : 0) << 1;
                lbpValue |= (image.get(i, j - 1)[0] >= center ? 1 : 0);

                lbpImage.put(i - 1, j - 1, lbpValue);
            }
        }

        Mat lbpHistogram = new Mat();
        Imgproc.calcHist(
            Arrays.asList(lbpImage),
            new MatOfInt(0),
            new Mat(),
            lbpHistogram,
            new MatOfInt(256),
            new MatOfFloat(0f, 256f)
        );
        Core.normalize(lbpHistogram, lbpHistogram, 1, 0, Core.NORM_L2);

        lbpImage.release();
        return lbpHistogram;
    }

    /**
     * Calculates HOG (Histogram of Oriented Gradients) for edge structure features.
     */
    private Mat calculateHOG(Mat image) {
        Mat resized = new Mat();
        Imgproc.resize(image, resized, new Size(64, 128));

        MatOfFloat descriptors = new MatOfFloat();
        hogDescriptor.compute(resized, descriptors);

        Mat hogMat = descriptors.clone();
        Core.normalize(hogMat, hogMat, 1, 0, Core.NORM_L2);

        resized.release();
        descriptors.release();

        return hogMat;
    }

    /**
     * Compares features using geometric mean with minimum score penalty.
     * Ensures ALL features must agree for high confidence score.
     *
     * @return Similarity score [0, 1]
     */
    private double compareFeatures(FaceFeatures f1, FaceFeatures f2) {
        double histScore = compareHistograms(f1.histogram, f2.histogram);
        double lbpScore = compareLBP(f1.lbp, f2.lbp);
        double hogScore = compareHOG(f1.hog, f2.hog);

        double minScore = Math.min(Math.min(histScore, lbpScore), hogScore);
        double geometricMean = Math.pow(histScore * lbpScore * hogScore, 1.0 / 3.0);

        return geometricMean * (0.5 + 0.5 * minScore);
    }

    /**
     * Compares histograms using chi-square distance.
     */
    private double compareHistograms(Mat hist1, Mat hist2) {
        double chiSquare = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CHISQR);
        double similarity = Math.exp(-chiSquare / 5.0);
        return Math.pow(similarity, 1.5);
    }

    /**
     * Compares LBP histograms using chi-square distance.
     */
    private double compareLBP(Mat lbp1, Mat lbp2) {
        double chiSquare = Imgproc.compareHist(lbp1, lbp2, Imgproc.HISTCMP_CHISQR);
        double similarity = Math.exp(-chiSquare / 3.0);
        return Math.pow(similarity, 2.0);
    }

    /**
     * Compares HOG descriptors using Euclidean distance.
     */
    private double compareHOG(Mat hog1, Mat hog2) {
        if (hog1.empty() || hog2.empty() || hog1.rows() != hog2.rows()) {
            return 0.0;
        }

        Mat diff = new Mat();
        Core.subtract(hog1, hog2, diff);
        double distance = Core.norm(diff, Core.NORM_L2);
        diff.release();

        double similarity = Math.exp(-distance / 20.0);
        return Math.pow(similarity, 2.5);
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
