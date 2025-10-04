package g1t1.scenes;

import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.models.scenes.PageController;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectionBoundingBox;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

class CameraRunnable implements Runnable {
    private final VideoCapture camera;
    private final ImageView display;
    private final FaceRecognitionService service;
    private final List<DetectionBoundingBox> boxes = new ArrayList<>();
    private final int msPerProcess; // milliseconds between processing steps
    private long previousTick; // previous timestamp frame was processed

    public CameraRunnable(ImageView display) {
        this.camera = new VideoCapture(FaceConfig.getInstance().getCameraIndex());
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

            for (DetectionBoundingBox boundingBox : this.boxes) {
                boundingBox.drawOnFrame(frame);
            }

            Platform.runLater(() -> {
                display.setImage(ImageUtils.matToImage(frame));
            });
        }
    }
}

public class DuringSessionViewController extends PageController {
    private ThreadWithRunnable<CameraRunnable> cameraDaemon;

    @FXML
    private ImageView ivCameraView;

    @FXML
    private void initialize() {
        ivCameraView.fitWidthProperty()
                .bind(ivCameraView.getParent().layoutBoundsProperty().map(bounds -> bounds.getWidth() - 350));
        ivCameraView.fitHeightProperty().bind(ivCameraView.getParent().layoutBoundsProperty().map(Bounds::getHeight));
    }

    @Override
    public void onMount() {
        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.start();
    }

    @Override
    public void onUnmount() {
        this.cameraDaemon.interrupt();
        AttendanceTaker.stop();
    }
}
