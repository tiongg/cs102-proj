package g1t1.opencv.services.recognition;

import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import g1t1.opencv.models.RecognitionResult;
import g1t1.opencv.services.preprocessing.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for face recognition algorithms using Template Method pattern.
 * Provides shared feature extraction (histogram, LBP, HOG) and comparison logic.
 * Subclasses specify which face region to analyze.
 */
public abstract class Recognizer {
    protected final FaceConfig config;
    protected final GrayscaleProcessor grayscale;
    protected final BilateralFilterProcessor bilateralFilter;
    protected final NormalizerProcessor normalizer;
    protected final CLAHEProcessor clahe;
    protected final ResizeProcessor resizer;
    protected final HOGDescriptor hogDescriptor;

    public Recognizer() {
        this.config = FaceConfig.getInstance();
        this.grayscale = new GrayscaleProcessor();
        this.bilateralFilter = new BilateralFilterProcessor();
        this.normalizer = new NormalizerProcessor();
        this.clahe = new CLAHEProcessor();
        this.resizer = new ResizeProcessor();
        this.hogDescriptor = new HOGDescriptor(
                new Size(64, 128),
                new Size(16, 16),
                new Size(8, 8),
                new Size(8, 8),
                9
        );
    }

    /**
     * Template method for face recognition workflow.
     */
    public final RecognitionResult recognize(Mat detectedFace, List<Recognisable> recognisableList) {
        if (detectedFace == null || detectedFace.empty() || recognisableList == null) {
            return null;
        }

        Mat processedFace = preprocessFace(detectedFace);
        Recognisable bestMatch = null;
        double bestConfidence = 0.0;

        for (Recognisable recognisable : recognisableList) {
            if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
                continue;
            }

            double confidence = compareWithRecognisable(processedFace, recognisable);
            if (confidence > bestConfidence && confidence >= config.getRecognitionThreshold()) {
                bestConfidence = confidence;
                bestMatch = recognisable;
            }
        }

        processedFace.release();

        if (bestMatch != null) {
            return new RecognitionResult(bestMatch, bestConfidence, null);
        }
        return null;
    }

    /**
     * Preprocess detected face using enhanced pipeline.
     * Pipeline: Grayscale -> Bilateral Filter -> Normalize -> CLAHE -> Resize
     */
    protected final Mat preprocessFace(Mat face) {
        Mat step1 = grayscale.process(face);
        Mat step2 = bilateralFilter.process(step1);
        Mat step3 = normalizer.process(step2);
        Mat step4 = clahe.process(step3);
        Mat result = resizer.process(step4);

        step1.release();
        step2.release();
        step3.release();
        step4.release();
        return result;
    }

    /**
     * Get best match without threshold filtering (for visual feedback).
     */
    public final RecognitionResult getBestMatch(Mat detectedFace, List<? extends Recognisable> recognisableList) {
        if (detectedFace == null || detectedFace.empty() || recognisableList == null) {
            return null;
        }

        Mat processedFace = preprocessFace(detectedFace);
        Recognisable bestMatch = null;
        double bestConfidence = 0.0;

        for (Recognisable recognisable : recognisableList) {
            if (recognisable.getFaceData() == null || recognisable.getFaceData().getFaceImages() == null) {
                continue;
            }

            double confidence = compareWithRecognisable(processedFace, recognisable);
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                bestMatch = recognisable;
            }
        }

        processedFace.release();

        if (bestMatch != null && bestConfidence > 0.0) {
            return new RecognitionResult(bestMatch, bestConfidence, null);
        }
        return null;
    }

    /**
     * Extracts all three feature types from face region.
     */
    protected final FaceFeatures extractFeatures(Mat face) {
        return new FaceFeatures(
                calculateHistogram(face),
                calculateLBP(face),
                calculateHOG(face)
        );
    }

    /**
     * Calculates intensity histogram (128 bins).
     */
    protected final Mat calculateHistogram(Mat image) {
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
    protected final Mat calculateLBP(Mat image) {
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
    protected final Mat calculateHOG(Mat image) {
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
    protected final double compareFeatures(FaceFeatures f1, FaceFeatures f2) {
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
    protected final double compareHistograms(Mat hist1, Mat hist2) {
        double chiSquare = Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CHISQR);
        double similarity = Math.exp(-chiSquare / 5.0);
        return Math.pow(similarity, 1.5);
    }

    /**
     * Compares LBP histograms using chi-square distance.
     */
    protected final double compareLBP(Mat lbp1, Mat lbp2) {
        double chiSquare = Imgproc.compareHist(lbp1, lbp2, Imgproc.HISTCMP_CHISQR);
        double similarity = Math.exp(-chiSquare / 3.0);
        return Math.pow(similarity, 2.0);
    }

    /**
     * Compares HOG descriptors using Euclidean distance.
     */
    protected final double compareHOG(Mat hog1, Mat hog2) {
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
     * Algorithm-specific comparison between processed face and student's enrolled data.
     * Subclasses implement their specific recognition algorithm here.
     */
    public abstract double compareWithRecognisable(Mat processedFace, Recognisable recognisable);

    /**
     * Container for face feature data.
     */
    protected static class FaceFeatures {
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
}
