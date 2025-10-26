package g1t1.scenes;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import g1t1.components.session.AttendanceStateList;
import g1t1.config.SettingsManager;
import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectionBoundingBox;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

class CameraRunnable implements Runnable {
    private final VideoCapture camera;
    private final ImageView display;
    private final BooleanProperty isTeacherInView;
    private final FaceRecognitionService service;
    private final List<DetectionBoundingBox> boxes = new ArrayList<>();
    private final int msPerProcess; // milliseconds between processing steps
    private long previousTick; // previous timestamp frame was processed

    public CameraRunnable(ImageView display, BooleanProperty isTeacherInView) {
        this.camera = new VideoCapture(FaceConfig.getInstance().getCameraIndex());
        this.isTeacherInView = isTeacherInView;
        this.display = display;
        this.service = FaceRecognitionService.getInstance();
        this.msPerProcess = 1 / FaceConfig.getInstance().getTargetFps() * 1000;
    }

    @Override
    public void run() {
        Mat frame = new Mat();
        while (camera.isOpened()) {
            if (Thread.currentThread().isInterrupted()) {
                camera.release();
                break;
            }

            if (!camera.read(frame) || frame.empty()) {
                continue;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - previousTick >= msPerProcess) {
                previousTick = currentTime;
                this.service.processFrame(frame, this.boxes);
            }

            boolean teacherFound = false;
            for (DetectionBoundingBox boundingBox : this.boxes) {
                boundingBox.drawOnFrame(frame);
                if (boundingBox.getIsTeacher()) {
                    teacherFound = true;
                }
            }
            this.isTeacherInView.set(teacherFound);

            Platform.runLater(() -> {
                display.setImage(ImageUtils.matToImage(frame));
            });
        }
    }
}

public class DuringSessionViewController extends PageController {
    private final BooleanProperty isTeacherInViewProperty = new SimpleBooleanProperty(false);
    private ThreadWithRunnable<CameraRunnable> cameraDaemon;
    private Timer remainingTimeTimer;

    @FXML
    private Label lblModule, lblSection, lblWeek, lblTimeStart, lblRemainingTime, lblPresent;

    @FXML
    private Button btnAdminPanel;

    @FXML
    private ImageView ivCameraView;

    @FXML
    private AttendanceStateList aslRecent;

    @FXML
    private AttendanceStateList aslStudents;

    @FXML
    private void initialize() {
        ivCameraView.fitWidthProperty()
                .bind(ivCameraView.getParent().layoutBoundsProperty().map(bounds -> bounds.getWidth() - 350));
        ivCameraView.fitHeightProperty().bind(ivCameraView.getParent().layoutBoundsProperty().map(Bounds::getHeight));
        btnAdminPanel.disableProperty().bind(this.isTeacherInViewProperty.not());
        aslRecent.attendances.bind(AttendanceTaker.recentlyMarked);
    }

    @Override
    public void onMount() {
        ClassSession session = AttendanceTaker.getCurrentSession();
        // Requires an ongoing attendence session!!
        if (session == null) {
            return;
        }
        assignLabels(session);
        aslStudents.attendances.clear();
        aslStudents.attendances.setAll(session.getStudentAttendance().values());

        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView, this.isTeacherInViewProperty);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.start();

        // Start timer to update remaining time every minute
        startRemainingTimeUpdater(session);
    }

    @Override
    public void onUnmount() {
        if (this.cameraDaemon != null) {
            this.cameraDaemon.interrupt();
        }
        if (this.remainingTimeTimer != null) {
            this.remainingTimeTimer.cancel();
            this.remainingTimeTimer = null;
        }
        AttendanceTaker.stop();
    }

    private void assignLabels(ClassSession session) {
        ModuleSection section = session.getModuleSection();

        this.lblModule.setText(section.getModule());
        this.lblSection.setText(section.getSection());

        this.lblWeek.setText(String.format("Week %d", session.getWeek()));
        this.lblTimeStart.setText(section.getStartTime());

        // Set initial remaining time
        updateRemainingTime(session);

        this.lblPresent.setText(String.format("%d / %d", 0, section.getStudents().size()));
    }

    private void startRemainingTimeUpdater(ClassSession session) {
        this.remainingTimeTimer = new Timer(true);
        this.remainingTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateRemainingTime(session);
            }
        }, 0, 60000); // Update every minute
    }

    private void updateRemainingTime(ClassSession session) {
        try {
            ModuleSection section = session.getModuleSection();
            String startTimeStr = section.getStartTime();

            // Parse start time (format: "HH:mm")
            LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

            // Get late threshold from settings
            int lateThresholdMins = SettingsManager.getInstance().getLateThresholdMinutes();

            // Calculate late cutoff time
            LocalTime lateTime = startTime.plusMinutes(lateThresholdMins);
            LocalTime now = LocalTime.now();

            Platform.runLater(() -> {
                if (now.isBefore(lateTime)) {
                    long remainingMins = Duration.between(now, lateTime).toMinutes();
                    lblRemainingTime.setText(remainingMins + " mins");
                } else {
                    lblRemainingTime.setText("Late period");
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating remaining time: " + e.getMessage());
            Platform.runLater(() -> {
                lblRemainingTime.setText("--");
            });
        }
    }
}
