package g1t1.components.register;

import g1t1.App;
import g1t1.components.Toast;
import g1t1.components.Toast.ToastType;
import g1t1.config.SettingsManager;
import g1t1.features.onboarding.FaceStepTracker;
import g1t1.models.interfaces.register.HasFaces;
import g1t1.models.scenes.Router;
import g1t1.models.users.FaceData;
import g1t1.models.users.RegisterTeacher;
import g1t1.opencv.models.FaceInFrame;
import g1t1.opencv.services.FaceDetector;
import g1t1.utils.EventEmitter;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import g1t1.utils.events.onboarding.PictureTakenEvent;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

class CameraRunnable implements Runnable {
    private static final int TARGET_SIZE = 256;
    private static final int MAX_FAILS = 100;
    private static final int CAPTURE_DELAY = 1000 / 5; // Maximum 30 fps

    public final EventEmitter<Object> emitter = new EventEmitter<>();
    private final VideoCapture camera;
    private final ImageView display;
    private final Object frameLock = new Object();
    private final Mat currentFrame = new Mat();
    private final BooleanProperty cameraFailure;
    private final BooleanProperty isTakingPictures;
    private final FaceDetector faceDetector;
    private final FaceStepTracker tracker;
    private long lastTimeTaken = 0;


    public CameraRunnable(ImageView display, BooleanProperty cameraFailure, BooleanProperty isTakingPictures, FaceStepTracker tracker) {
        this.camera = SettingsManager.getInstance().getConfiguredCamera();
        this.display = display;
        this.cameraFailure = cameraFailure;
        this.isTakingPictures = isTakingPictures;
        this.faceDetector = new FaceDetector();
        this.tracker = tracker;
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
            Mat croppedFrame = ImageUtils.cropToFit(frame, TARGET_SIZE, TARGET_SIZE);

            // Update the shared frame
            synchronized (frameLock) {
                croppedFrame.copyTo(currentFrame);
            }

            Platform.runLater(() -> {
                display.setImage(ImageUtils.matToImage(croppedFrame));

                if (!isTakingPictures.getValue()) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                long deltaTime = currentTime - this.lastTimeTaken;
                if (deltaTime >= CAPTURE_DELAY) {
                    byte[] face = getFaceInFrame();
                    // No face found
                    if (face.length <= 0) {
                        return;
                    }
                    emitter.emit(new PictureTakenEvent(face));
                    this.lastTimeTaken = currentTime;
                }
            });
        }
        camera.release();
        if (consecutiveFailures >= MAX_FAILS) {
            this.cameraFailure.set(true);
        }
    }

    // To take thumbnail picture
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

    public byte[] getFaceInFrame() {
        synchronized (frameLock) {
            if (currentFrame.empty()) {
                return new byte[]{};
            }

            FaceInFrame faceRegion = faceDetector.getFaceFromMatrix(currentFrame, 0);

            if (faceRegion == null) {
                return new byte[]{};
            }

            // Failed face validation
            if (!this.tracker.currentStep.getValue().isRegionValid(faceRegion)) {
                return new byte[]{};
            }

            // Resize to 200x200
            Mat resizedFace = new Mat();
            Imgproc.resize(faceRegion.faceBounds(), resizedFace, new Size(200, 200));

            // Encode to byte array
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", resizedFace, buffer);
            byte[] result = buffer.toArray();

            // Cleanup
            faceRegion.faceBounds().release();
            resizedFace.release();
            buffer.release();
            return result;
        }
    }
}

public class FaceDetails extends Tab implements RegistrationStep<HasFaces> {
    private final int REQUIRED_PICTURE_COUNT = 100;

    private final BooleanProperty isValid = new SimpleBooleanProperty(true);
    private final BooleanProperty cameraFailure = new SimpleBooleanProperty(false);
    private final BooleanProperty isTakingPictures = new SimpleBooleanProperty(false);
    private final ListProperty<byte[]> photosTaken = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final FileChooser fileChooser = new FileChooser();
    private final FaceDetector faceDetector = new FaceDetector();
    private final FaceStepTracker stepTracker;
    private ThreadWithRunnable<CameraRunnable> cameraDaemon;
    private byte[] thumbnailImage;
    @FXML
    private ProgressBar pbPercentDone;
    @FXML
    private Label lblScanText;
    @FXML
    private ImageView ivCameraView;
    @FXML
    private Label lblCameraError;
    @FXML
    private Label lblOnboardType;
    @FXML
    private Button btnStartScan;

    public FaceDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetails.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.stepTracker = new FaceStepTracker(this.photosTaken);

        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            reset();
        });

        fileChooser.setTitle("Select Face Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        pbPercentDone.progressProperty().bind(photosTaken.sizeProperty().divide((float) REQUIRED_PICTURE_COUNT));
        isValid.bind(photosTaken.map(x -> x.size() >= REQUIRED_PICTURE_COUNT));
        lblCameraError.visibleProperty().bind(cameraFailure);
        lblScanText.textProperty().bind(this.stepTracker.instructionProperty());
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

        CameraRunnable cameraThread = new CameraRunnable(this.ivCameraView, this.cameraFailure, this.isTakingPictures, this.stepTracker);
        this.cameraDaemon = new ThreadWithRunnable<>(cameraThread);
        this.cameraDaemon.setDaemon(true);
        this.cameraDaemon.getRunnable().emitter.subscribe(PictureTakenEvent.class, this::handlePictureEvent);
        this.cameraDaemon.start();
    }

    @Override
    public void reset() {
        this.photosTaken.clear();
        this.btnStartScan.visibleProperty().set(true);
        thumbnailImage = null;
    }

    @FXML
    public void startTakingPictures() {
        this.btnStartScan.visibleProperty().set(false);
        this.isTakingPictures.set(true);
    }

    public void handlePictureEvent(PictureTakenEvent e) {
        // First image taken is thumbnail
        if (thumbnailImage == null) {
            byte[] frame = this.cameraDaemon.getRunnable().getCurrentFrame();
            thumbnailImage = frame.clone();
        }
        this.photosTaken.add(e.picture());

        if (this.photosTaken.sizeProperty().intValue() >= 100) {
            Toast.show("All pictures taken!", ToastType.SUCCESS);
            this.isTakingPictures.set(false);
        }
    }

    @FXML
    public void importImages() {
        List<File> filesSelected = this.fileChooser.showOpenMultipleDialog(App.getRootStage());
        if (filesSelected == null || filesSelected.size() <= 0) {
            Toast.show("No files selected!", ToastType.ERROR);
            return;
        }
        int imported = 0;
        for (File file : filesSelected) {
            Mat imageMat = null;
            Mat faceRegion = null;
            MatOfByte matOfByte = null;
            MatOfByte buffer = null;
            try (FileInputStream fsIn = new FileInputStream(file)) {
                byte[] imageRaw = fsIn.readAllBytes();
                // Extract face from imported image
                matOfByte = new MatOfByte(imageRaw);
                imageMat = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
                FaceInFrame inFrame = faceDetector.getFaceFromMatrix(imageMat, 0);
                if (inFrame == null || inFrame.faceBounds() == null) {
                    continue;
                }
                faceRegion = inFrame.faceBounds();

                // Re-encode it back to byte[]
                buffer = new MatOfByte();
                Imgcodecs.imencode(".png", faceRegion, buffer);
                byte[] image = buffer.toArray();
                this.photosTaken.add(image);
                if (thumbnailImage == null) {
                    // Crop the thumbnail to match takePicture() behavior
                    Mat croppedThumbnail = ImageUtils.cropToFit(imageMat, 256, 256);
                    MatOfByte thumbnailBuffer = new MatOfByte();
                    Imgcodecs.imencode(".png", croppedThumbnail, thumbnailBuffer);
                    thumbnailImage = thumbnailBuffer.toArray();

                    // Cleanup thumbnail resources
                    croppedThumbnail.release();
                    thumbnailBuffer.release();
                }
                imported++;
            } catch (IOException e) {
                Toast.show(String.format("Error loading file %s", file.getAbsolutePath()), ToastType.ERROR);
            } finally {
                if (imageMat != null)
                    imageMat.release();
                if (faceRegion != null)
                    faceRegion.release();
                if (matOfByte != null)
                    matOfByte.release();
                if (buffer != null)
                    buffer.release();
            }
        }
        Toast.show(String.format("Successfully imported %d images!", imported), ToastType.SUCCESS);
    }
}
