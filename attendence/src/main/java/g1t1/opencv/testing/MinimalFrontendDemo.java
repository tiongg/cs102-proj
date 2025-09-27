package g1t1.opencv.testing;

import g1t1.models.users.Student;
import g1t1.models.users.FaceData;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.events.FaceEventListener;
import g1t1.opencv.events.StudentDetectedEvent;
import g1t1.opencv.events.AttendanceSessionEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal JavaFX Frontend Demo
 *
 * PURPOSE: Demonstrates REAL JavaFX integration with face recognition system.
 * Shows how UI components update in real-time based on face detection events.
 *
 * HOW TO RUN:
 * cd attendence && mvn compile javafx:run -Djavafx.mainClass="g1t1.opencv.testing.MinimalFrontendDemo"
 */
public class MinimalFrontendDemo extends Application {

    // UI Components
    private Button startButton;
    private Button stopButton;
    private Label statusLabel;
    private ListView<String> attendanceListView;
    private Label totalCountLabel;
    private TextArea logTextArea;

    // Face Recognition Service
    private FaceRecognitionService faceService;
    private List<Student> enrolledStudents;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Face Recognition Frontend Demo");

        // Initialize face recognition service
        faceService = FaceRecognitionService.getInstance();
        loadEnrolledStudents();

        // Create UI
        VBox root = createUI();
        setupEventHandlers();

        // Show window
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Log initial state
        logMessage("Frontend initialized with " + enrolledStudents.size() + " enrolled students");
    }

    private VBox createUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Title
        Label titleLabel = new Label("Face Recognition System - Live Demo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Control buttons
        HBox buttonBox = new HBox(10);
        startButton = new Button("Start Recognition");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        stopButton = new Button("Stop Recognition");
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        stopButton.setDisable(true);

        buttonBox.getChildren().addAll(startButton, stopButton);

        // Status
        statusLabel = new Label("Status: Ready to start");
        statusLabel.setStyle("-fx-font-weight: bold;");

        // Attendance list
        Label attendanceLabel = new Label("Detected Students:");
        attendanceListView = new ListView<>();
        attendanceListView.setPrefHeight(150);

        // Total count
        totalCountLabel = new Label("Total Detected: 0");
        totalCountLabel.setStyle("-fx-font-weight: bold;");

        // Log area
        Label logLabel = new Label("System Log:");
        logTextArea = new TextArea();
        logTextArea.setPrefHeight(120);
        logTextArea.setEditable(false);

        root.getChildren().addAll(
            titleLabel,
            buttonBox,
            statusLabel,
            attendanceLabel,
            attendanceListView,
            totalCountLabel,
            logLabel,
            logTextArea
        );

        return root;
    }

    private void setupEventHandlers() {
        // Start button
        startButton.setOnAction(e -> {
            if (enrolledStudents.isEmpty()) {
                showAlert("No students enrolled. Add photos to test-photos/[name]/ folders.");
                return;
            }

            // Set up event listener BEFORE starting service
            faceService.getEventEmitter().addListener(new FrontendEventListener());

            // Start recognition service
            faceService.start(enrolledStudents);

            // Update UI state
            Platform.runLater(() -> {
                startButton.setDisable(true);
                stopButton.setDisable(false);
                statusLabel.setText("Status: Recognition ACTIVE - Position yourself in front of camera");
                statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
                logMessage("Face recognition started");
            });
        });

        // Stop button
        stopButton.setOnAction(e -> {
            faceService.stop();

            Platform.runLater(() -> {
                startButton.setDisable(false);
                stopButton.setDisable(true);
                statusLabel.setText("Status: Recognition STOPPED");
                statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                logMessage("Face recognition stopped");
            });
        });
    }

    /**
     * Real JavaFX Event Listener - Updates UI components in real-time
     */
    private class FrontendEventListener implements FaceEventListener {

        @Override
        public void onStudentDetected(StudentDetectedEvent event) {
            Platform.runLater(() -> {
                String studentInfo = String.format("%s (ID: %s) - %.1f%% confidence",
                    event.getStudent().getName(),
                    event.getStudent().getId().toString(),
                    event.getConfidence());

                // Update attendance list (avoid duplicates by checking if already exists)
                if (!attendanceListView.getItems().contains(studentInfo)) {
                    attendanceListView.getItems().add(studentInfo);
                }

                logMessage("DETECTED: " + studentInfo);
            });
        }

        @Override
        public void onAttendanceSessionUpdated(AttendanceSessionEvent event) {
            Platform.runLater(() -> {
                totalCountLabel.setText("Total Detected: " + event.getSession().getTotalStudentsDetected());
                logMessage("Session updated: " + event.getEventType());
            });
        }
    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logTextArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadEnrolledStudents() {
        enrolledStudents = new ArrayList<>();
        File baseDir = new File("test-photos");

        if (!baseDir.exists()) return;

        File[] studentDirs = baseDir.listFiles(File::isDirectory);
        if (studentDirs != null) {
            for (File studentDir : studentDirs) {
                try {
                    List<byte[]> photos = loadPhotosFromFolder(studentDir.getPath());
                    if (!photos.isEmpty()) {
                        String name = capitalize(studentDir.getName());
                        String id = "S" + String.format("%03d", enrolledStudents.size() + 1);
                        enrolledStudents.add(createStudent(id, name, photos));
                    }
                } catch (IOException e) {
                    System.err.println("Error loading photos for " + studentDir.getName() + ": " + e.getMessage());
                }
            }
        }
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
                } catch (IOException e) {
                    System.out.println("[WARNING] Skipped: " + file.getName());
                }
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

    @Override
    public void stop() throws Exception {
        // Clean shutdown
        if (faceService != null && faceService.isRunning()) {
            faceService.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}