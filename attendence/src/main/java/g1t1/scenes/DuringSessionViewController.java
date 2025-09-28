package g1t1.scenes;

import g1t1.models.scenes.PageController;
import g1t1.models.users.Student;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectionBoundingBox;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import javafx.application.Platform;
import javafx.fxml.FXML;
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

    public CameraRunnable(ImageView display) {
        this.camera = new VideoCapture(FaceConfig.getInstance().getCameraIndex());
        this.display = display;
        this.service = FaceRecognitionService.getInstance();
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

            this.service.processFrame(frame, this.boxes);
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

    @Override
    public void onMount() {
        List<Student> enrolled = new ArrayList<>();
        FaceRecognitionService.getInstance().start(enrolled);

        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.start();
    }

    @Override
    public void onUnmount() {
        this.cameraDaemon.interrupt();
        FaceRecognitionService.getInstance().stop();
    }
}
