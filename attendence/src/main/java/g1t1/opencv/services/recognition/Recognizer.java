package g1t1.opencv.services.recognition;

import g1t1.models.users.Student;
import g1t1.opencv.config.*;
import g1t1.opencv.models.*;
import g1t1.opencv.services.preprocessing.*;
import org.opencv.core.*;

import java.util.*;

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
    public final RecognitionResult recognize(Mat detectedFace, List<Student> enrolledStudents) {
        if (detectedFace == null || detectedFace.empty() || enrolledStudents == null) {
            return null;
        }

        Mat processedFace = preprocessFace(detectedFace);
        Student bestMatch = null;
        double bestConfidence = 0.0;

        for (Student student : enrolledStudents) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            double confidence = compareWithStudent(processedFace, student);
            if (confidence > bestConfidence && confidence >= config.getRecognitionThreshold()) {
                bestConfidence = confidence;
                bestMatch = student;
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
        Mat step2 = normalizer.process(step1);
        Mat step3 = histogramEqualizer.process(step2);
        Mat result = resizer.process(step3);

        step1.release();
        step2.release();
        step3.release();
        return result;
    }

    /**
     * Get best match without threshold filtering (for visual feedback).
     */
    public final RecognitionResult getBestMatch(Mat detectedFace, List<Student> enrolledStudents) {
        if (detectedFace == null || detectedFace.empty() || enrolledStudents == null) {
            return null;
        }

        Mat processedFace = preprocessFace(detectedFace);
        Student bestMatch = null;
        double bestConfidence = 0.0;

        for (Student student : enrolledStudents) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            double confidence = compareWithStudent(processedFace, student);
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
    public abstract double compareWithStudent(Mat processedFace, Student student);
}