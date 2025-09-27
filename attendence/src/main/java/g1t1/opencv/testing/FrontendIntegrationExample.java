package g1t1.opencv.testing;

import g1t1.models.users.Student;
import g1t1.models.users.FaceData;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
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
 * Frontend Integration Example
 *
 * PURPOSE: Shows JavaFX developers how to integrate face recognition into their UI.
 *
 * INTEGRATION PATTERNS DEMONSTRATED:
 * - Service lifecycle management
 * - Event-driven UI updates
 * - Asynchronous operations
 * - Configuration management
 * - Error handling
 *
 * JAVAFX INTEGRATION GUIDE:
 * 1. Use Platform.runLater() for UI updates in event listeners
 * 2. Run face recognition on background thread
 * 3. Bind recognition status to UI controls
 * 4. Handle camera access errors gracefully
 *
 * HOW TO RUN:
 * mvn compile exec:java -Dexec.mainClass="g1t1.opencv.testing.FrontendIntegrationExample"
 */
public class FrontendIntegrationExample {

    public static void main(String[] args) {
        System.out.println("=== FRONTEND INTEGRATION EXAMPLE ===");
        System.out.println("Demonstrating JavaFX integration patterns");
        System.out.println();

        try {
            new FrontendIntegrationExample().runExample();
        } catch (Exception e) {
            System.out.println("[ERROR] Integration example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main integration example
     */
    public void runExample() throws IOException, InterruptedException {
        System.out.println("INTEGRATION PATTERNS DEMONSTRATED:");
        System.out.println("   1. Service lifecycle management");
        System.out.println("   2. Event-driven UI updates");
        System.out.println("   3. Configuration management");
        System.out.println("   4. Error handling");
        System.out.println();

        // Load students (this would come from your database)
        List<Student> enrolledStudents = loadEnrolledStudents();
        if (enrolledStudents.isEmpty()) {
            System.out.println("[WARNING] No enrolled students found");
            return;
        }

        // Pattern 1: Service Lifecycle Management
        demonstrateServiceLifecycle(enrolledStudents);

        // Pattern 2: Event-Driven UI Updates
        demonstrateEventHandling(enrolledStudents);

        // Pattern 3: Configuration Management
        demonstrateConfigurationManagement();

        // Pattern 4: Error Handling
        demonstrateErrorHandling();

        System.out.println("Integration examples completed!");
        System.out.println("Use these patterns in your JavaFX application");
    }

    /**
     * PATTERN 1: Service Lifecycle Management
     * Shows how to properly start/stop recognition service
     */
    private void demonstrateServiceLifecycle(List<Student> students) throws InterruptedException {
        System.out.println("PATTERN 1: Service Lifecycle Management");
        System.out.println("   // JavaFX Controller Code Example:");
        System.out.println("   @FXML private Button startButton;");
        System.out.println("   @FXML private Button stopButton;");
        System.out.println("   @FXML private Label statusLabel;");
        System.out.println();

        FaceRecognitionService service = FaceRecognitionService.getInstance();

        // Start recognition (bind to start button)
        System.out.println("   Starting recognition service...");
        service.start(students);

        // Update UI status (this would be in Platform.runLater())
        System.out.println("   [SUCCESS] Service started - " + service.getEnrolledStudentCount() + " students enrolled");
        System.out.println("   [INFO] In JavaFX: Platform.runLater(() -> statusLabel.setText(\"Recognition Active\"));");

        Thread.sleep(2000);

        // Stop recognition (bind to stop button)
        System.out.println("   Stopping recognition service...");
        service.stop();
        System.out.println("   [SUCCESS] Service stopped");
        System.out.println("   [INFO] In JavaFX: Platform.runLater(() -> statusLabel.setText(\"Recognition Stopped\"));");
        System.out.println();
    }

    /**
     * PATTERN 2: Event-Driven UI Updates
     * Shows how to handle student detection events in UI
     */
    private void demonstrateEventHandling(List<Student> students) throws InterruptedException {
        System.out.println("PATTERN 2: Event-Driven UI Updates");
        System.out.println("   // JavaFX Event Listener Example:");
        System.out.println("   private class UIEventListener implements FaceEventListener {");
        System.out.println("       @Override");
        System.out.println("       public void onStudentDetected(StudentDetectedEvent event) {");
        System.out.println("           Platform.runLater(() -> updateAttendanceList(event.getStudent()));");
        System.out.println("       }");
        System.out.println("   }");
        System.out.println();

        FaceRecognitionService service = FaceRecognitionService.getInstance();

        // Create UI event listener
        UISimulatorEventListener uiListener = new UISimulatorEventListener();
        service.getEventEmitter().addListener(uiListener);

        // Start recognition
        service.start(students);
        System.out.println("   Running recognition for 8 seconds to demonstrate events...");

        Thread.sleep(8000);

        service.stop();

        System.out.println("   [INFO] Events received: " + uiListener.getEventCount() + " detection, " + uiListener.getSessionEventCount() + " session");
        System.out.println("   [INFO] Each event would update your JavaFX UI components");
        System.out.println();
    }

    /**
     * PATTERN 3: Configuration Management
     * Shows how to integrate with settings/preferences
     */
    private void demonstrateConfigurationManagement() {
        System.out.println("PATTERN 3: Configuration Management");
        System.out.println("   // JavaFX Settings Integration Example:");
        System.out.println("   @FXML private Slider sensitivitySlider;");
        System.out.println("   @FXML private CheckBox maskDetectionCheckBox;");
        System.out.println();

        FaceConfig config = FaceConfig.getInstance();

        // Show current configuration
        System.out.println("   Current Configuration:");
        System.out.println("   - Recognition Threshold: " + config.getRecognitionThreshold() + "%");
        System.out.println("   - Target FPS: " + config.getTargetFps());
        System.out.println("   - Camera Resolution: " + config.getCameraWidth() + "x" + config.getCameraHeight());
        System.out.println("   - Mask Detection: " + (config.isMaskDetectionEnabled() ? "Enabled" : "Disabled"));

        // Demonstrate dynamic configuration
        System.out.println("   Demonstrating dynamic threshold adjustment:");
        double originalThreshold = config.getRecognitionThreshold();

        config.setRecognitionThreshold(60.0);
        System.out.println("   [SUCCESS] Threshold changed to: " + config.getRecognitionThreshold() + "%");
        System.out.println("   [INFO] In JavaFX: sensitivitySlider.valueProperty().addListener((obs, old, val) -> {");
        System.out.println("       config.setRecognitionThreshold(val.doubleValue());");
        System.out.println("   });");

        // Restore original
        config.setRecognitionThreshold(originalThreshold);
        System.out.println("   Restored to: " + config.getRecognitionThreshold() + "%");
        System.out.println();
    }

    /**
     * PATTERN 4: Error Handling
     * Shows how to handle common errors gracefully
     */
    private void demonstrateErrorHandling() {
        System.out.println("PATTERN 4: Error Handling");
        System.out.println("   // JavaFX Error Handling Example:");
        System.out.println("   try {");
        System.out.println("       service.start(students);");
        System.out.println("   } catch (Exception e) {");
        System.out.println("       Platform.runLater(() -> showErrorDialog(\"Camera access failed\"));");
        System.out.println("   }");
        System.out.println();

        System.out.println("   Common error scenarios to handle:");
        System.out.println("   [SUCCESS] Camera access denied - Show permission dialog");
        System.out.println("   [SUCCESS] No students enrolled - Show enrollment prompt");
        System.out.println("   [SUCCESS] Service already running - Update UI state");
        System.out.println("   [SUCCESS] Configuration errors - Reset to defaults");
        System.out.println();

        // Demonstrate error detection
        FaceRecognitionService service = FaceRecognitionService.getInstance();

        // Example: Try to start without students
        try {
            service.start(new ArrayList<>());
        } catch (Exception e) {
            System.out.println("   [ERROR] Caught error: " + e.getClass().getSimpleName());
            System.out.println("   [INFO] In JavaFX: Display user-friendly error message");
        }

        System.out.println();
    }

    // Helper methods and classes

    private List<Student> loadEnrolledStudents() throws IOException {
        List<Student> students = new ArrayList<>();
        File baseDir = new File("test-photos");

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

    private List<byte[]> loadPhotosFromFolder(String folderPath) throws IOException {
        List<byte[]> photos = new ArrayList<>();
        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) ->
            name.toLowerCase().matches(".*\\.(jpg|jpeg)$"));

        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                try {
                    photos.add(Files.readAllBytes(file.toPath()));
                } catch (IOException ignored) {}
            }
        }

        return photos;
    }

    private Student createStudent(String id, String name, List<byte[]> photos) {
        Student student = new Student(id, name, "CS102", "T01",
                                    name.toLowerCase() + "@school.edu", "12345678");
        FaceData faceData = new FaceData();
        faceData.setFaceImages(photos);
        student.setFaceData(faceData);
        return student;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Example event listener that simulates JavaFX UI updates
     */
    private static class UISimulatorEventListener implements FaceEventListener {
        private int eventCount = 0;
        private int sessionEventCount = 0;

        @Override
        public void onStudentDetected(StudentDetectedEvent event) {
            eventCount++;

            // Simulate JavaFX UI update
            System.out.println("   [UI] Update #" + eventCount + ": " +
                             event.getStudent().getName() + " detected (" +
                             String.format("%.1f%%", event.getConfidence()) + ")");

            // This would be wrapped in Platform.runLater() in real JavaFX:
            // Platform.runLater(() -> {
            //     attendanceListView.getItems().add(event.getStudent().getName());
            //     confidenceLabel.setText(String.format("%.1f%%", event.getConfidence()));
            // });
        }

        @Override
        public void onAttendanceSessionUpdated(AttendanceSessionEvent event) {
            sessionEventCount++;

            // Simulate JavaFX session update
            System.out.println("   [SESSION] Update #" + sessionEventCount + ": " +
                             event.getEventType() + " (Total: " +
                             event.getSession().getTotalStudentsDetected() + " students)");

            // This would be wrapped in Platform.runLater() in real JavaFX:
            // Platform.runLater(() -> {
            //     sessionStatusLabel.setText(event.getEventType());
            //     totalCountLabel.setText(String.valueOf(event.getSession().getTotalStudentsDetected()));
            // });
        }

        public int getEventCount() {
            return eventCount;
        }

        public int getSessionEventCount() {
            return sessionEventCount;
        }
    }
}