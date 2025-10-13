package g1t1.opencv.services.recognition;

import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.Recognisable;
import g1t1.opencv.models.RecognitionResult;
import g1t1.opencv.services.preprocessing.GrayscaleProcessor;
import g1t1.opencv.services.preprocessing.HistogramEqualizationProcessor;
import g1t1.opencv.services.preprocessing.NormalizerProcessor;
import g1t1.opencv.services.preprocessing.ResizeProcessor;
import org.opencv.core.Mat;

import java.util.List;

/**
 * Abstract base class for face recognition algorithms using Template Method pattern.
 */
public abstract class Recognizer {
    protected final FaceConfig config;
    protected final GrayscaleProcessor grayscale;
    protected final NormalizerProcessor normalizer;
    protected final HistogramEqualizationProcessor histogramEqualizer;
    protected final ResizeProcessor resizer;

    public Recognizer() {
        this.config = FaceConfig.getInstance();
        this.grayscale = new GrayscaleProcessor();
        this.normalizer = new NormalizerProcessor();
        this.histogramEqualizer = new HistogramEqualizationProcessor();
        this.resizer = new ResizeProcessor();
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
     */
    protected final Mat preprocessFace(Mat face) {
        Mat step1 = grayscale.process(face);

        // Apply slight Gaussian blur to reduce noise and improve histogram stability
        Mat step1b = new Mat();
        org.opencv.imgproc.Imgproc.GaussianBlur(step1, step1b, new org.opencv.core.Size(3, 3), 0.5);

        Mat step2 = normalizer.process(step1b);
        Mat step3 = histogramEqualizer.process(step2);
        Mat result = resizer.process(step3);

        step1.release();
        step1b.release();
        step2.release();
        step3.release();
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

        for (Recognisable student : recognisableList) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            double confidence = compareWithRecognisable(processedFace, student);
            if (confidence > bestConfidence) {
                bestConfidence = confidence;
                bestMatch = student;
            }
        }

        processedFace.release();

        if (bestMatch != null && bestConfidence > 0.0) {
            return new RecognitionResult(bestMatch, bestConfidence, null);
        }
        return null;
    }

    /**
     * Algorithm-specific comparison between processed face and student's enrolled data.
     * Subclasses implement their specific recognition algorithm here.
     */
    public abstract double compareWithRecognisable(Mat processedFace, Recognisable recognisable);
}