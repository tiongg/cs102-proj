package g1t1.opencv.services.recognition;

import g1t1.features.logger.AppLogger;
import g1t1.models.users.Student;
import g1t1.opencv.config.FaceConfig;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Face recognizer that handles masked faces by focusing on upper face region.
 * Optimized with pre-computed histograms for better performance.
 */
public class MaskAwareRecognizer extends Recognizer {

    // Cache for pre-computed upper face histograms per student
    private final Map<String, List<Mat>> studentUpperHistograms = new ConcurrentHashMap<>();
    private final Map<String, Double> bestKnownSimilarities = new ConcurrentHashMap<>();

    /**
     * Pre-compute upper face histograms for all enrolled students.
     */
    public void precomputeEnrollmentData(List<Student> enrolledStudents) {
        long startTime = System.currentTimeMillis();
        studentUpperHistograms.clear();
        bestKnownSimilarities.clear();

        for (Student student : enrolledStudents) {
            if (student.getFaceData() == null || student.getFaceData().getFaceImages() == null) {
                continue;
            }

            List<Mat> histograms = new ArrayList<>();
            List<byte[]> faceImages = student.getFaceData().getFaceImages();

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
                studentUpperHistograms.put(student.getId().toString(), histograms);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Pre-computed mask-aware enrollment histograms for " + enrolledStudents.size() +
                         " students in " + duration + "ms");
        }
    }

    @Override
    public double compareWithStudent(Mat processedFace, Student student) {
        String studentId = student.getId().toString();
        List<Mat> precomputedHistograms = studentUpperHistograms.get(studentId);

        if (precomputedHistograms == null || precomputedHistograms.isEmpty()) {
            // Fallback to original method if no pre-computed data
            return fallbackCompareWithStudent(processedFace, student);
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
        bestKnownSimilarities.put(studentId, confidencePercentage);

        return confidencePercentage;
    }

    /**
     * Fallback method for when pre-computed data is not available
     */
    private double fallbackCompareWithStudent(Mat processedFace, Student student) {
        Mat upperFaceRegion = extractUpperFaceRegion(processedFace);
        Mat faceHistogram = calculateHistogram(upperFaceRegion);
        double bestSimilarity = 0.0;

        if (student.getFaceData() != null && student.getFaceData().getFaceImages() != null) {
            for (byte[] imageData : student.getFaceData().getFaceImages()) {
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
        for (List<Mat> histograms : studentUpperHistograms.values()) {
            for (Mat histogram : histograms) {
                histogram.release();
            }
        }
        studentUpperHistograms.clear();
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