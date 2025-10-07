package g1t1.components.register;

import g1t1.models.interfaces.register.HasFaces;
import g1t1.models.scenes.Router;
import g1t1.models.users.FaceData;
import g1t1.models.users.RegisterTeacher;
import g1t1.opencv.config.FaceConfig;
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
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

class CameraRunnable implements Runnable {
    private static final int TARGET_SIZE = 256;
    private static final int MAX_FAILS = 100;
    private final VideoCapture camera;
    private final ImageView display;
    private final Object frameLock = new Object();
    private final Mat currentFrame = new Mat();
    private final BooleanProperty cameraFailure;

    public CameraRunnable(ImageView display, BooleanProperty cameraFailure) {
        this.camera = new VideoCapture(FaceConfig.getInstance().getCameraIndex());
        this.display = display;
        this.cameraFailure = cameraFailure;
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
                return buffer.toArray();
            }
        }
        return new byte[]{};
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

        lblTakenPictures.textProperty().bind(photosTaken.map(x -> String.format("%d / %d", x.size(), REQUIRED_PICTURE_COUNT)));
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
        byte[] frame = this.cameraDaemon.getRunnable().getCurrentFrame();
        // First image taken is thumbnail
        if (thumbnailImage == null) {
            // Clone incase processing pipeline modifies original buffer
            thumbnailImage = frame.clone();
        }
        this.photosTaken.add(frame);
    }
}
