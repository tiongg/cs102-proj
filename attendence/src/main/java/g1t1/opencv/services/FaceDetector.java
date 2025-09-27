package g1t1.opencv.services;

import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectedFace;
import nu.pattern.OpenCV;
import org.bytedeco.javacpp.Loader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * Face detector using OpenCV CascadeClassifier for face detection.
 * Detects multiple faces in frames and assigns tracking IDs.
 */
public class FaceDetector {
    private CascadeClassifier faceCascade;
    private FaceConfig config;
    private int nextFaceId;

    public FaceDetector() {
        this.config = FaceConfig.getInstance();
        this.nextFaceId = 1;
        initialize();
    }

    /**
     * Initialize OpenCV and load face cascade classifier.
     */
    private void initialize() {
        try {
            OpenCV.loadLocally();
            loadCascadeClassifier();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize face detector", e);
        }
    }

    private void loadCascadeClassifier() {
        try {
            String classifierName = "haarcascade_frontalface_alt.xml";
            File classifierFile = Loader.extractResource(classifierName, null, "classifier", ".xml");

            if (classifierFile == null || classifierFile.length() <= 0) {
                URL url = URI.create("https://raw.githubusercontent.com/opencv/opencv/4.x/data/haarcascades/haarcascade_frontalface_alt.xml").toURL();
                classifierFile = Loader.cacheResource(url);
            }

            faceCascade = new CascadeClassifier(classifierFile.getAbsolutePath());

            if (faceCascade.empty()) {
                throw new RuntimeException("Failed to load HaarCascade classifier from: " + classifierFile.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load HaarCascade classifier", e);
        }
    }

    /**
     * Detect faces in the given frame.
     * Returns list of detected faces with bounding boxes and IDs.
     */
    public List<DetectedFace> detectFaces(Mat frame) {
        if (frame == null || frame.empty()) {
            return new ArrayList<>();
        }

        Mat grayFrame = preprocessFrame(frame);

        // Placeholder detection for testing camera functionality
        List<DetectedFace> detectedFaces = new ArrayList<>();

        if (faceCascade.empty()) {
            // No cascade loaded - return empty list for camera testing
        } else {
            // Detect faces using HaarCascade
            MatOfRect faceDetections = new MatOfRect();
            faceCascade.detectMultiScale(
                grayFrame,
                faceDetections,
                config.getScaleFactor(),
                config.getMinNeighbors(),
                0,
                new Size(config.getMinSize(), config.getMinSize()),
                new Size()
            );

            Rect[] faces = faceDetections.toArray();
            for (Rect face : faces) {
                DetectedFace detectedFace = new DetectedFace(face, 1.0, nextFaceId++);
                detectedFaces.add(detectedFace);
            }

            faceDetections.release();
        }

        // Cleanup
        grayFrame.release();

        return detectedFaces;
    }

    /**
     * Extract face region from frame based on detection.
     */
    public Mat extractFaceRegion(Mat frame, DetectedFace detection) {
        if (frame == null || detection == null || detection.getBoundingBox() == null) {
            return new Mat();
        }

        Rect boundingBox = detection.getBoundingBox();

        // Ensure bounding box is within frame boundaries
        int x = Math.max(0, boundingBox.x);
        int y = Math.max(0, boundingBox.y);
        int width = Math.min(boundingBox.width, frame.cols() - x);
        int height = Math.min(boundingBox.height, frame.rows() - y);

        if (width <= 0 || height <= 0) {
            return new Mat();
        }

        Rect safeBounds = new Rect(x, y, width, height);
        return new Mat(frame, safeBounds);
    }

    /**
     * Preprocess frame for detection (convert to grayscale).
     */
    private Mat preprocessFrame(Mat frame) {
        Mat grayFrame = new Mat();

        if (frame.channels() == 3) {
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        } else {
            frame.copyTo(grayFrame);
        }

        return grayFrame;
    }

    /**
     * Check if detector is properly initialized.
     */
    public boolean isInitialized() {
        return faceCascade != null;
    }

    /**
     * Get detection statistics for monitoring.
     */
    public String getDetectionStats() {
        return String.format("FaceDetector{initialized=%s, nextFaceId=%d}",
                           isInitialized(), nextFaceId);
    }
}