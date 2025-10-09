package g1t1.components.register;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import g1t1.models.interfaces.register.HasFaces;
import g1t1.models.scenes.Router;
import g1t1.models.users.FaceData;
import g1t1.models.users.RegisterTeacher;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectedFace;
import g1t1.opencv.services.FaceDetector;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;

class CameraRunnable implements Runnable {
    private static final int TARGET_SIZE = 256;
    private static final int MAX_FAILS = 100;
    private final VideoCapture camera;
    private final ImageView display;
    private final Object frameLock = new Object();
    private final Mat currentFrame = new Mat();
    private final BooleanProperty cameraFailure;
    private final FaceDetector faceDetector;

    public CameraRunnable(ImageView display, BooleanProperty cameraFailure) {
        this.camera = new VideoCapture(FaceConfig.getInstance().getCameraIndex());
        this.display = display;
        this.cameraFailure = cameraFailure;
        this.faceDetector = new FaceDetector();
    }

    @Override
    public void run() {
        Mat frame = new Mat();
        int consecutiveFailures = 0;
        this.cameraFailure.set(false);
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
            Mat croppedFrame = ImageUtils.cropToFit(frame, TARGET_SIZE, TARGET_SIZE);

            // Update the shared frame
            synchronized (frameLock) {
                croppedFrame.copyTo(currentFrame);
            }

            Platform.runLater(() -> {
                display.setImage(ImageUtils.matToImage(croppedFrame));
            });
        }
        camera.release();
        if (consecutiveFailures >= MAX_FAILS) {
            cameraFailure.set(true);
        }
    }

    public byte[] getCurrentFrame() {
        synchronized (frameLock) {
            if (!currentFrame.empty()) {
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".png", currentFrame, buffer);
                // File file = new File("test-photos/test.png");
                // try (FileOutputStream writer = new FileOutputStream(file)) {
                // writer.write(buffer.toArray());
                // } catch (IOException e) {
                // }
                return buffer.toArray();
            }
        }
        return new byte[] {};
    }

    public byte[] getFaceInFrame() {
        synchronized (frameLock) {
            if (currentFrame.empty()) {
                return new byte[] {};
            }

            List<DetectedFace> detectedFaces = faceDetector.detectFaces(currentFrame);
            if (detectedFaces.isEmpty()) {
                return new byte[] {};
            }

            DetectedFace detectedFace = detectedFaces.getFirst();
            Rect boundingBox = detectedFace.getBoundingBox();

            if (boundingBox == null) {
                return new byte[] {};
            }

            // Ensure bounding box is within frame boundaries
            int x = Math.max(0, boundingBox.x - 20);
            int y = Math.max(0, boundingBox.y - 20);
            int width = Math.min(boundingBox.width + 40, currentFrame.cols() - x);
            int height = Math.min(boundingBox.height + 40, currentFrame.rows() - y);

            if (width <= 0 || height <= 0) {
                return new byte[] {};
            }

            // Extract face region
            Rect safeBounds = new Rect(x, y, width, height);
            Mat faceRegion = new Mat(currentFrame, safeBounds);

            // Encode to byte array
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", faceRegion, buffer);
            byte[] result = buffer.toArray();

            // Cleanup
            faceRegion.release();
            buffer.release();

            return result;
        }
    }
}

public class FaceDetails extends Tab implements RegistrationStep<HasFaces> {
    private final BooleanProperty isValid = new SimpleBooleanProperty(true);
    private final BooleanProperty cameraFailure = new SimpleBooleanProperty(false);
    private final ListProperty<byte[]> photosTaken = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final int REQUIRED_PICTURE_COUNT = 15;
    private ThreadWithRunnable<CameraRunnable> cameraDaemon;
    private byte[] thumbnailImage;

    @FXML
    private Label lblTakenPictures;
    @FXML
    private ImageView ivCameraView;
    @FXML
    private Label lblCameraError;
    @FXML
    private Label lblOnboardType;

    public FaceDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetails.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            reset();
        });

        lblTakenPictures.textProperty()
                .bind(photosTaken.map(x -> String.format("%d / %d", x.size(), REQUIRED_PICTURE_COUNT)));
        isValid.bind(photosTaken.map(x -> x.size() >= REQUIRED_PICTURE_COUNT));
        lblCameraError.visibleProperty().bind(cameraFailure);
    }

    @Override
    public BooleanProperty validProperty() {
        return this.isValid;
    }

    @Override
    public void setProperty(HasFaces target) {
        target.setFaceData(new FaceData(this.photosTaken.get()));
        target.setThumbnail(this.thumbnailImage);
    }

    @Override
    public void onUnmount() {
        cameraDaemon.interrupt();
        cameraDaemon = null;
    }

    @Override
    public void onMount(Object target) {
        if (target instanceof RegisterTeacher) {
            lblOnboardType.setText("Teacher");
        }

        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView, this.cameraFailure);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.start();
    }

    @Override
    public void reset() {
        this.photosTaken.clear();
        thumbnailImage = null;
    }

    public void takePicture() {
        // First image taken is thumbnail
        if (thumbnailImage == null) {
            byte[] frame = this.cameraDaemon.getRunnable().getCurrentFrame();
            thumbnailImage = frame.clone();
        }
        byte[] faceInFrame = this.cameraDaemon.getRunnable().getFaceInFrame();
        if (faceInFrame.length <= 0) {
            System.out.println("Error taking photo! No face");
            return;
        }
        this.photosTaken.add(faceInFrame);
    }
}
