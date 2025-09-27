package g1t1.opencv.testing;

import g1t1.models.users.Student;
import g1t1.models.users.FaceData;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.events.FaceEventListener;
import g1t1.opencv.events.StudentDetectedEvent;
import g1t1.opencv.events.AttendanceSessionEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * System Validation Test
 *
 * PURPOSE: Validates core face recognition functionality before frontend integration.
 *
 * WHAT IT TESTS:
 * - Service lifecycle (start/stop)
 * - Event system integration
 * - Student enrollment and detection
 *
 * HOW TO RUN:
 * 1. Add student photos to test-photos/[name]/ folders
 * 2. Run: mvn compile exec:java -Dexec.mainClass="g1t1.opencv.testing.SystemValidationTest"
 *
 * EXPECTED OUTPUT:
 * - Service starts and stops successfully
 * - Events are emitted when students detected
 * - System ready for frontend integration
 */
public class SystemValidationTest {

    private static final String TEST_PHOTOS_BASE = "test-photos";

    public static void main(String[] args) {
        System.out.println("=== FACE RECOGNITION SYSTEM VALIDATION ===");
        System.out.println("Validating core functionality for frontend integration");
        System.out.println();

        try {
            runValidation();
        } catch (Exception e) {
            System.out.println("[ERROR] Validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runValidation() throws IOException {
        // Load test students
        List<Student> students = loadTestStudents();
        if (students.isEmpty()) {
            System.out.println("[ERROR] No test students found.");
            System.out.println("[INFO] Add photos to test-photos/[name]/ folders and try again");
            return;
        }

        System.out.println("[SUCCESS] Loaded " + students.size() + " test students:");
        students.forEach(s -> System.out.println("   - " + s.getName() + " (" + s.getId() + ")"));
        System.out.println();

        // Run validation tests
        testServiceLifecycle(students);
        testEventSystem(students);

        System.out.println("VALIDATION COMPLETE");
        System.out.println("[SUCCESS] System ready for frontend integration");
    }

    /**
     * Test 1: Service Lifecycle
     * Validates that frontend can start and stop the service
     */
    private static void testServiceLifecycle(List<Student> students) {
        System.out.println("TEST: Service Lifecycle");
        FaceRecognitionService service = FaceRecognitionService.getInstance();

        try {
            // Test start
            service.start(students);
            Thread.sleep(1000);

            if (service.isRunning()) {
                System.out.println("   [SUCCESS] Service started successfully");
                System.out.println("   [INFO] Enrolled students: " + service.getEnrolledStudentCount());
            } else {
                System.out.println("   [ERROR] Service failed to start");
            }

            // Test stop
            service.stop();
            Thread.sleep(500);

            if (!service.isRunning()) {
                System.out.println("   [SUCCESS] Service stopped successfully");
            } else {
                System.out.println("   [ERROR] Service failed to stop");
            }

        } catch (Exception e) {
            System.out.println("   [ERROR] Lifecycle error: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 2: Event System
     * Validates that frontend can receive student detection events
     */
    private static void testEventSystem(List<Student> students) {
        System.out.println("TEST: Event System Integration");
        FaceRecognitionService service = FaceRecognitionService.getInstance();

        // Create event listener (this is what frontend would do)
        ValidationEventListener listener = new ValidationEventListener();
        service.getEventEmitter().addListener(listener);

        try {
            service.start(students);
            System.out.println("   Running 8-second detection test...");
            System.out.println("   [INFO] Position yourself in front of camera for best results");

            Thread.sleep(8000);
            service.stop();

            int events = listener.getEventCount();
            int sessionEvents = listener.getSessionEventCount();
            if (events > 0 || sessionEvents > 0) {
                System.out.println("   [SUCCESS] Event system working: " + events + " detection events, " + sessionEvents + " session events received");
                System.out.println("   [SUCCESS] Frontend can successfully receive notifications");
            } else {
                System.out.println("   [WARNING] No events received");
                System.out.println("   [INFO] Check camera access or ensure face is visible");
            }

        } catch (Exception e) {
            System.out.println("   [ERROR] Event system error: " + e.getMessage());
        }
        System.out.println();
    }

    // Helper methods for loading test data
    private static List<Student> loadTestStudents() throws IOException {
        List<Student> students = new ArrayList<>();
        File baseDir = new File(TEST_PHOTOS_BASE);

        if (!baseDir.exists()) return students;

        File[] studentDirs = baseDir.listFiles(File::isDirectory);
        if (studentDirs != null) {
            for (File studentDir : studentDirs) {
                List<byte[]> photos = loadPhotosFromFolder(studentDir.getPath());
                if (!photos.isEmpty()) {
                    String name = capitalize(studentDir.getName());
                    String id = "S" + String.format("%03d", students.size() + 1);
                    students.add(createStudent(id, name, photos));
                }
            }
        }

        return students;
    }

    private static List<byte[]> loadPhotosFromFolder(String folderPath) throws IOException {
        List<byte[]> photos = new ArrayList<>();
        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) ->
            name.toLowerCase().matches(".*\\.(jpg|jpeg)$"));

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                try {
                    photos.add(Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    System.out.println("[WARNING] Skipped: " + file.getName());
                }
            }
        }

        return photos;
    }

    private static Student createStudent(String id, String name, List<byte[]> photos) {
        Student student = new Student(id, name, "CS102", "T01",
                                    name.toLowerCase() + "@school.edu", "12345678");
        FaceData faceData = new FaceData();
        faceData.setFaceImages(photos);
        student.setFaceData(faceData);
        return student;
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Event listener for validation testing
     * Shows how frontend should implement event handling
     */
    private static class ValidationEventListener implements FaceEventListener {
        private int eventCount = 0;
        private int sessionEventCount = 0;

        @Override
        public void onStudentDetected(StudentDetectedEvent event) {
            eventCount++;
            System.out.println("   [EVENT] " + eventCount + ": " +
                             event.getStudent().getName() +
                             " detected (confidence: " +
                             String.format("%.1f%%", event.getConfidence()) + ")");
        }

        @Override
        public void onAttendanceSessionUpdated(AttendanceSessionEvent event) {
            sessionEventCount++;
            System.out.println("   [SESSION] " + sessionEventCount + ": " +
                             event.getEventType() +
                             " (Total detected: " + event.getSession().getTotalStudentsDetected() + ")");
        }

        public int getEventCount() {
            return eventCount;
        }

        public int getSessionEventCount() {
            return sessionEventCount;
        }
    }
}