package g1t1.scenes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import g1t1.models.scenes.PageController;
import g1t1.models.users.FaceData;
import g1t1.models.users.Student;
import g1t1.opencv.FaceRecognitionService;
import g1t1.opencv.config.FaceConfig;
import g1t1.opencv.models.DetectionBoundingBox;
import g1t1.utils.ImageUtils;
import g1t1.utils.ThreadWithRunnable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;

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
        List<Student> enrolled = loadEnrolledStudents();
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

    private List<Student> loadEnrolledStudents() {
        List<Student> enrolledStudents = new ArrayList<>();
        File baseDir = new File("test-photos");

        if (!baseDir.exists())
            return enrolledStudents;

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
        return enrolledStudents;
    }

    private List<byte[]> loadPhotosFromFolder(String folderPath) throws IOException {
        List<byte[]> photos = new ArrayList<>();
        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg)$"));

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
        Student student = new Student(id, name, "CS102", "T01", name.toLowerCase() + "@school.edu", "12345678");
        FaceData faceData = new FaceData();
        faceData.setFaceImages(photos);
        student.setFaceData(faceData);
        return student;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
