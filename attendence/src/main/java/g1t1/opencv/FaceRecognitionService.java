package g1t1.opencv;

import g1t1.config.SettingsManager;
import g1t1.features.logger.AppLogger;
import g1t1.models.users.Student;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.*;
import g1t1.opencv.services.FaceDetector;
import g1t1.opencv.services.MaskDetector;
import g1t1.opencv.services.liveness.LivenessChecker;
import g1t1.opencv.services.recognition.HistogramRecognizer;
import g1t1.opencv.services.recognition.MaskAwareRecognizer;
import g1t1.opencv.services.recognition.Recognizer;
import g1t1.utils.EventEmitter;
import g1t1.utils.events.opencv.AttendanceSessionEvent;
import g1t1.utils.events.opencv.StudentDetectedEvent;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for face recognition system. Frontend calls start() and
 * stop() methods. Provides global event emitter for system-wide event
 * listening.
 */
public class FaceRecognitionService {
    private static FaceRecognitionService instance;
    private final int FRAME_SKIP_INTERVAL = 2;
    private final long RECOGNITION_CACHE_DURATION = 2000;

    private EventEmitter<Object> eventEmitter;
    private boolean isRunning;
    private List<? extends Recognisable> recognisableObjects;
    private FaceDetector faceDetector;
    private HistogramRecognizer histogramRecognizer;
    private MaskAwareRecognizer maskAwareRecognizer;
    private MaskDetector maskDetector;
    private LivenessChecker livenessChecker;
    private AttendanceSession currentSession;
    private Set<String> loggedUsers;
    private Map<String, Boolean> maskCache;
    private Map<String, Long> lastMaskCheckTime;
    private int frameSkipCounter = 0;
    private List<DetectedFace> cachedFaces = new ArrayList<>();
    private Map<String, RecognitionResult> recognitionCache = new ConcurrentHashMap<>();
    private Map<String, Long> recognitionCacheTime = new ConcurrentHashMap<>();

    private FaceRecognitionService() {
        this.eventEmitter = new EventEmitter<>();
        this.isRunning = false;
        this.faceDetector = new FaceDetector();
        this.histogramRecognizer = new HistogramRecognizer();
        this.maskAwareRecognizer = new MaskAwareRecognizer();
        this.maskDetector = new MaskDetector();
        this.livenessChecker = new LivenessChecker();
    }

    /**
     * Get singleton instance.
     */
    public static FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }

    /**
     * Start face recognition with enrolled students. Frontend calls this to begin
     * attendance session.
     */
    public void start(List<? extends Recognisable> recognisableObjects) {
        if (isRunning) {
            return;
        }

        this.recognisableObjects = recognisableObjects;
        this.isRunning = true;
        this.currentSession = new AttendanceSession();
        this.loggedUsers = new HashSet<>();
        this.maskCache = new HashMap<>();
        this.lastMaskCheckTime = new HashMap<>();

        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.logf("Face recognition started with %d recognisable objects", recognisableObjects.size());
        }

        histogramRecognizer.precomputeEnrollmentData(recognisableObjects);
        maskAwareRecognizer.precomputeEnrollmentData(recognisableObjects);

        // Emit session started event
        eventEmitter.emit(new AttendanceSessionEvent(currentSession, AttendanceSessionEvent.SESSION_STARTED));
    }

    /**
     * Stop face recognition and cleanup resources. Frontend calls this to end
     * attendance session.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        this.isRunning = false;
        this.recognisableObjects = null;

        // End current session
        if (currentSession != null) {
            currentSession.endSession();
            eventEmitter.emit(new AttendanceSessionEvent(currentSession, AttendanceSessionEvent.SESSION_ENDED));

            if (FaceConfig.getInstance().isLoggingEnabled()) {
                AppLogger.logf("Face recognition stopped - Session summary: %d unique faces detected",
                    loggedUsers != null ? loggedUsers.size() : 0);
            }
        }

        histogramRecognizer.cleanup();
        maskAwareRecognizer.cleanup();
        cachedFaces.clear();
        recognitionCache.clear();
        recognitionCacheTime.clear();
        frameSkipCounter = 0;
    }

    /**
     * Get global event emitter for listening to face recognition events. Other
     * parts of the system use this to listen for student detections.
     */
    public EventEmitter<Object> getEventEmitter() {
        return eventEmitter;
    }

    /**
     * Check if face recognition is currently running.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get number of enrolled students (for debugging/monitoring).
     */
    public int getEnrolledStudentCount() {
        return recognisableObjects != null ? recognisableObjects.size() : 0;
    }

    /**
     * Get current attendance session.
     */
    public AttendanceSession getCurrentSession() {
        return currentSession;
    }

    public void processFrame(Mat frame, List<DetectionBoundingBox> boxes) {
        List<DetectedFace> detectedFaces;
        if (frameSkipCounter % FRAME_SKIP_INTERVAL == 0) {
            Mat smallFrame = new Mat();
            double scaleFactor = 1;
            Imgproc.resize(frame, smallFrame, new Size(frame.cols() * scaleFactor, frame.rows() * scaleFactor));

            List<DetectedFace> smallDetections = faceDetector.detectFaces(smallFrame);
            detectedFaces = scaleDetectedFaces(smallDetections, 1.0 / scaleFactor);
            cachedFaces = detectedFaces;
            frameSkipCounter = 0;

            smallFrame.release();
        } else {
            detectedFaces = cachedFaces;
        }
        frameSkipCounter++;

        // Draw detection boxes and try recognition
        boxes.clear();

        for (DetectedFace detectedFace : detectedFaces) {
            Rect box = detectedFace.getBoundingBox();
            if (box != null) {
                DetectionBoundingBox boundingBox = new DetectionBoundingBox(new Point(box.x, box.y),
                        new Point(box.x + box.width, box.y + box.height), 2);

                Mat faceRegion = faceDetector.extractFaceRegion(frame, detectedFace);
                if (!faceRegion.empty()) {
                    boolean isLive = true;
                    String livenessInfo = "";

                    if (FaceConfig.getInstance().isLivenessEnabled()) {
                        LivenessResult livenessResult = livenessChecker.checkLiveness(faceRegion);
                        isLive = livenessResult.isLive();
                        livenessInfo = isLive ? "" : " (PHOTO?)";
                    }

                    if (isLive) {
                        // Check for mask detection
                        String faceIdForRecognizer = "face_" + String.valueOf(detectedFace.getFaceId());
                        boolean hasMask = checkMaskStatus(faceRegion, faceIdForRecognizer);
                        String maskInfo = hasMask ? " [MASK]" : "";

                        RecognitionResult result = getCachedOrNewRecognition(faceRegion, recognisableObjects,
                                String.valueOf(detectedFace.getFaceId()));

                        if (result != null) {
                            Recognisable recognisedObject = result.getMatchedObject();
                            double confidence = result.getConfidence();

                            if (confidence >= SettingsManager.getInstance().getDetectionThreshold()) {
                                boundingBox.setRecognised(recognisedObject, livenessInfo + maskInfo, confidence);

                                handleRecognitionResult(recognisedObject, confidence);
                            }
                        }
                    } else {
                        // Liveliness check failed
                        boundingBox.setPicture();
                    }

                    boxes.add(boundingBox);
                    faceRegion.release();
                }
            }
        }
    }

    private void handleRecognitionResult(Recognisable recognisedObject, double confidence) {
        String recognitionId = recognisedObject.getRecognitionId();

        boolean isNewRecognition = !currentSession.isStudentDetected(recognitionId);
        double previousMaxConfidence = currentSession.getMaxConfidenceForStudent(recognitionId);

        currentSession.updateRecognisedDetection(recognisedObject, confidence);

        if (confidence >= FaceConfig.getInstance().getRecognitionThreshold() && !loggedUsers.contains(recognitionId)) {
            loggedUsers.add(recognitionId);
            if (FaceConfig.getInstance().isLoggingEnabled()) {
                AppLogger.logf("Face detected: %s (ID: %s, Confidence: %.2f%%)",
                    recognisedObject.getName(), recognitionId, confidence);
            }
        }

        if ((isNewRecognition || (confidence - previousMaxConfidence >= 1.0))
                && (recognisedObject instanceof Student student)) {
            eventEmitter.emit(new StudentDetectedEvent(student, confidence));
            eventEmitter.emit(new AttendanceSessionEvent(currentSession, AttendanceSessionEvent.STUDENT_UPDATED));
        }
    }

    private boolean checkMaskStatus(Mat faceRegion, String recognitionId) {
        if (!FaceConfig.getInstance().isMaskDetectionEnabled()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        boolean needsCheck = false;

        if (!maskCache.containsKey(recognitionId)) {
            needsCheck = true;
        } else {
            Long lastCheck = lastMaskCheckTime.get(recognitionId);
            if (lastCheck == null || (currentTime - lastCheck) > 500) { // 500ms - more responsive
                needsCheck = true;
            }
        }

        boolean hasMask;
        if (needsCheck) {
            hasMask = maskDetector.detectMask(faceRegion);
            maskCache.put(recognitionId, hasMask);
            lastMaskCheckTime.put(recognitionId, currentTime);
        } else {
            hasMask = maskCache.get(recognitionId);
        }

        return hasMask;
    }

    private Recognizer selectRecognizer(Mat faceRegion, String recognitionId) {
        boolean hasMask = checkMaskStatus(faceRegion, recognitionId);
        return hasMask ? maskAwareRecognizer : histogramRecognizer;
    }

    /**
     * Scale detected faces coordinates from small frame to full resolution.
     */
    private List<DetectedFace> scaleDetectedFaces(List<DetectedFace> smallFaces, double scale) {
        List<DetectedFace> scaledFaces = new ArrayList<>();
        for (DetectedFace smallFace : smallFaces) {
            Rect smallBox = smallFace.getBoundingBox();
            if (smallBox != null) {
                Rect scaledBox = new Rect((int) (smallBox.x * scale), (int) (smallBox.y * scale),
                        (int) (smallBox.width * scale), (int) (smallBox.height * scale));
                DetectedFace scaledFace = new DetectedFace(scaledBox, smallFace.getConfidence(), smallFace.getFaceId());
                scaledFaces.add(scaledFace);
            }
        }
        return scaledFaces;
    }

    /**
     * Get cached recognition result if still valid, otherwise perform recognition.
     */
    private RecognitionResult getCachedOrNewRecognition(Mat faceRegion,
                                                        List<? extends Recognisable> recognisableObjects, String faceId) {
        long currentTime = System.currentTimeMillis();

        // Check if we have a valid cached result
        RecognitionResult cached = recognitionCache.get(faceId);
        Long cacheTime = recognitionCacheTime.get(faceId);

        if (cached != null && cacheTime != null && (currentTime - cacheTime) < RECOGNITION_CACHE_DURATION) {
            return cached; // Return cached result
        }

        // Perform new recognition
        String faceIdForRecognizer = "face_" + faceId;
        Recognizer selectedRecognizer = selectRecognizer(faceRegion, faceIdForRecognizer);
        RecognitionResult result = selectedRecognizer.getBestMatch(faceRegion, recognisableObjects);

        // Cache the result
        if (result != null) {
            recognitionCache.put(faceId, result);
            recognitionCacheTime.put(faceId, currentTime);
        }

        return result;
    }
}