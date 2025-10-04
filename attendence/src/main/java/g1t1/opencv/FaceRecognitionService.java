package g1t1.opencv;

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
    private List<Student> enrolledStudents;
    private FaceDetector faceDetector;
    private HistogramRecognizer histogramRecognizer;
    private MaskAwareRecognizer maskAwareRecognizer;
    private MaskDetector maskDetector;
    private LivenessChecker livenessChecker;
    private AttendanceSession currentSession;
    private Set<String> loggedStudents;
    private Map<String, Boolean> maskCache;
    private Map<String, Long> lastMaskCheckTime;
    private int frameSkipCounter = 0;
    private List<DetectedFace> cachedFaces = new ArrayList<>();
    private Map<String, RecognitionResult> recognitionCache = new ConcurrentHashMap<>();
    private Map<String, Long> recognitionCacheTime = new ConcurrentHashMap<>();

    private FaceRecognitionService() {
        this.eventEmitter = new EventEmitter();
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
    public void start(List<Student> students) {
        if (isRunning) {
            return;
        }

        this.enrolledStudents = students;
        this.isRunning = true;
        this.currentSession = new AttendanceSession();
        this.loggedStudents = new HashSet<>();
        this.maskCache = new HashMap<>();
        this.lastMaskCheckTime = new HashMap<>();

        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Face recognition started with " + students.size() + " enrolled students");
        }

        histogramRecognizer.precomputeEnrollmentData(students);
        maskAwareRecognizer.precomputeEnrollmentData(students);

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
        this.enrolledStudents = null;

        // End current session
        if (currentSession != null) {
            currentSession.endSession();
            eventEmitter.emit(new AttendanceSessionEvent(currentSession, AttendanceSessionEvent.SESSION_ENDED));
        }

        histogramRecognizer.cleanup();
        maskAwareRecognizer.cleanup();
        cachedFaces.clear();
        recognitionCache.clear();
        recognitionCacheTime.clear();
        frameSkipCounter = 0;

        if (FaceConfig.getInstance().isLoggingEnabled()) {
            AppLogger.log("Face recognition stopped");
        }
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
        return enrolledStudents != null ? enrolledStudents.size() : 0;
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
            double scaleFactor = 0.5;
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
                        RecognitionResult result = getCachedOrNewRecognition(faceRegion, enrolledStudents,
                                String.valueOf(detectedFace.getFaceId()));

                        if (result != null) {
                            Student recognizedStudent = result.getMatchedStudent();
                            double confidence = result.getConfidence();

                            if (confidence >= 5) {
                                boundingBox.setStudent(recognizedStudent.getName(), livenessInfo, confidence);

                                handleRecognitionResult(recognizedStudent, confidence);
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

    private void handleRecognitionResult(Student student, double confidence) {
        String studentId = student.getId().toString();

        boolean isNewStudent = !currentSession.isStudentDetected(studentId);
        double previousMaxConfidence = currentSession.getMaxConfidenceForStudent(studentId);

        currentSession.updateStudentDetection(student, confidence);

        if (confidence >= FaceConfig.getInstance().getRecognitionThreshold() && !loggedStudents.contains(studentId)) {
            loggedStudents.add(studentId);
            if (FaceConfig.getInstance().isLoggingEnabled()) {
                AppLogger.log("Student detected: " + student.getName() + " (ID: " + studentId + ", Section: "
                        + student.getModuleSection() + ")");
            }
        }

        if (isNewStudent || (confidence - previousMaxConfidence >= 1.0)) {
            eventEmitter.emit(new StudentDetectedEvent(student, confidence));
            eventEmitter.emit(new AttendanceSessionEvent(currentSession, AttendanceSessionEvent.STUDENT_UPDATED));
        }
    }

    private Recognizer selectRecognizer(Mat faceRegion, String studentId) {
        if (!FaceConfig.getInstance().isMaskDetectionEnabled()) {
            return histogramRecognizer;
        }

        long currentTime = System.currentTimeMillis();
        boolean needsCheck = false;

        if (!maskCache.containsKey(studentId)) {
            needsCheck = true;
        } else {
            Long lastCheck = lastMaskCheckTime.get(studentId);
            if (lastCheck == null || (currentTime - lastCheck) > 3000) { // 3 seconds
                needsCheck = true;
            }
        }

        boolean hasMask;
        if (needsCheck) {
            hasMask = maskDetector.detectMask(faceRegion);
            maskCache.put(studentId, hasMask);
            lastMaskCheckTime.put(studentId, currentTime);
        } else {
            hasMask = maskCache.get(studentId);
        }

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
    private RecognitionResult getCachedOrNewRecognition(Mat faceRegion, List<Student> students, String faceId) {
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
        RecognitionResult result = selectedRecognizer.getBestMatch(faceRegion, students);

        // Cache the result
        if (result != null) {
            recognitionCache.put(faceId, result);
            recognitionCacheTime.put(faceId, currentTime);
        }

        return result;
    }
}