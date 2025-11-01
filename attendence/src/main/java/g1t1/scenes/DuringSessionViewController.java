package g1t1.scenes;

import g1t1.components.session.AttendanceStateList;
import g1t1.config.SettingsManager;
import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectionBoundingBox;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

class CameraRunnable implements Runnable {
    private static final int MAX_FAILS = 100;

    private final VideoCapture camera;
    private final ImageView display;
    private final BooleanProperty isTeacherInView;
    private final BooleanProperty cameraFailure;
    private final FaceRecognitionService service;
    private final List<DetectionBoundingBox> boxes = new ArrayList<>();
    private final int msPerProcess;
    private long previousTick;

    public CameraRunnable(ImageView display, BooleanProperty isTeacherInView, BooleanProperty cameraFailure) {
        this.camera = SettingsManager.getInstance().getConfiguredCamera();
        this.isTeacherInView = isTeacherInView;
        this.display = display;
        this.service = FaceRecognitionService.getInstance();
        this.msPerProcess = 1000 / FaceConfig.getInstance().getTargetFps();
        this.cameraFailure = cameraFailure;
    }

    @Override
    public void run() {
        Mat frame = new Mat();
        int consecutiveFailures = 0;
        this.cameraFailure.set(false);

        // Immediately set failure if camera didn't open
        if (!camera.isOpened()) {
            this.cameraFailure.set(true);
            return;
        }

        while (camera.isOpened()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (!camera.read(frame) || frame.empty()) {
                // Stop the loop if it fails too often
                consecutiveFailures++;
                if (consecutiveFailures >= MAX_FAILS) {
                    break;
                }
                continue;
            }
            consecutiveFailures = 0;

            long currentTime = System.currentTimeMillis();
            if (currentTime - previousTick >= msPerProcess) {
                previousTick = currentTime;
                if (this.service.isRunning()) {
                    this.service.processFrame(frame, this.boxes);
                } else {
                    this.boxes.clear();
                    break;  // Exit camera loop, to avoid race condition upon face recognition stopped
                }
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

        camera.release();
        if (consecutiveFailures >= MAX_FAILS) {
            this.cameraFailure.set(true);
        }
    }
}

public class DuringSessionViewController extends PageController {
    private final BooleanProperty cameraFailure = new SimpleBooleanProperty(false);
    private final BooleanProperty isTeacherInViewProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty isAdminPanelOpen = new SimpleBooleanProperty(false);
    private ThreadWithRunnable<CameraRunnable> cameraDaemon;
    private Timeline countdownTimer;
    private LocalDateTime sessionStartTime;

    @FXML
    private Label lblModule, lblSection, lblWeek, lblTimeStart,
            lblRemainingTime, lblPresent, lblConfirmStudentName;

    @FXML
    private Button btnAdminPanel;

    @FXML
    private ImageView ivCameraView;

    @FXML
    private AttendanceStateList aslRecent;

    @FXML
    private AttendanceStateList aslStudents;

    @FXML
    private AttendanceStateList aslMarkableStudents;

    @FXML
    private VBox vbxDefaultPanel, vbxAdminPanel;

    @FXML
    private HBox hbxConfirmStudent;

    @FXML
    private Label lblCameraError;

    @FXML
    private void initialize() {
        ivCameraView.fitWidthProperty().bind(ivCameraView.getParent().layoutBoundsProperty().map(Bounds::getWidth));
        ivCameraView.fitHeightProperty().bind(ivCameraView.getParent().layoutBoundsProperty().map(Bounds::getHeight));
        this.btnAdminPanel.disableProperty().bind(this.isTeacherInViewProperty.not());
        this.aslRecent.attendances.bind(AttendanceTaker.recentlyMarked);

        this.hbxConfirmStudent.visibleProperty().bind(AttendanceTaker.needsConfirmation.isNotNull());
        this.lblConfirmStudentName.textProperty().bind(AttendanceTaker.needsConfirmation.map(x -> {
            if (x == null) {
                return "";
            }
            return String.format("Confirm: %s", x.getStudent().getName());
        }));

        this.vbxDefaultPanel.visibleProperty().bind(isAdminPanelOpen.not());
        this.vbxAdminPanel.visibleProperty().bind(isAdminPanelOpen);
        lblCameraError.visibleProperty().bind(cameraFailure);
    }

    @Override
    public void onMount() {
        ClassSession session = AttendanceTaker.getCurrentSession();
        if (session == null) {
            return;
        }

        assignLabels(session);
        aslStudents.attendances.setAll(session.getStudentAttendance().values());
        aslMarkableStudents.attendances.setAll(session.getStudentAttendance().values());

        this.lblPresent.textProperty().bind(AttendanceTaker.studentsPresent
                .map(x -> String.format("%d / %d", x.intValue(), session.getStudentAttendance().size())));

        // Start countdown timer
        sessionStartTime = session.getStartTime();
        startCountdownTimer();

        // Start camera
        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView, this.isTeacherInViewProperty, this.cameraFailure);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.start();
    }

    @Override
    public void onUnmount() {
        if (this.cameraDaemon != null) {
            this.cameraDaemon.interrupt();
        }

        if (this.countdownTimer != null) {
            this.countdownTimer.stop();
        }

        this.lblPresent.textProperty().unbind();
        this.cameraDaemon = null;
        this.countdownTimer = null;
        this.isAdminPanelOpen.set(false);
    }

    private void assignLabels(ClassSession session) {
        ModuleSection section = session.getModuleSection();

        this.lblModule.setText(section.getModule());
        this.lblSection.setText(section.getSection());
        this.lblWeek.setText(String.format("Week %d", session.getWeek()));
        this.lblTimeStart.setText(section.getStartTime());
    }

    private void startCountdownTimer() {
        // Update every second for smooth countdown
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateRemainingTime();
        }));
        countdownTimer.setCycleCount(Animation.INDEFINITE);
        countdownTimer.play();

        // Initial update
        updateRemainingTime();
    }

    private void updateRemainingTime() {
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = ChronoUnit.MINUTES.between(sessionStartTime, now);
        long minutesRemaining = SettingsManager.getInstance().getLateThresholdMinutes() - minutesElapsed;

        if (minutesRemaining <= 0) {
            lblRemainingTime.setText("Late!");
            lblRemainingTime.setStyle("-fx-text-fill: #FF0000; -fx-font-weight: bold;");
        } else {
            lblRemainingTime.setText(String.format("%d min%s", minutesRemaining, minutesRemaining == 1 ? "" : "s"));
            lblRemainingTime.setStyle(""); // Reset style
        }
    }

    @FXML
    public void toggleAdminPanel() {
        this.isAdminPanelOpen.set(!this.isAdminPanelOpen.get());
    }

    @FXML
    public void stopSession() {
        AttendanceTaker.stop();
        Router.changePage(PageName.PastRecords);
    }

    @FXML
    public void acceptStudentAttendance() {
        AttendanceTaker.acceptPrompt();
    }

    @FXML
    public void rejectStudentAttendance() {
        AttendanceTaker.rejectPrompt();
    }
}