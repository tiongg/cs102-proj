package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.config.AppSettings;
import g1t1.config.SettingsManager;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.opencv.videoio.VideoCapture;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsViewController extends PageController {

    @FXML
    private TextField tfDetectionThreshold;

    @FXML
    private TextField tfAutomarkThreshold;

    @FXML
    private TextField tfLateThreshold;

    @FXML
    private ComboBox<Integer> cbCameraDevice;

    @FXML
    private TextField tfLogPath;

    @FXML
    private Button btnDetectCameras;

    @FXML
    private void initialize() {
        try {
            // Populate with default camera indices 0-5
            cbCameraDevice.getItems().addAll(0, 1, 2, 3, 4, 5);

            // Load current settings
            loadSettings();
        } catch (Exception e) {
            System.err.println("Error initializing Settings page: " + e.getMessage());
            e.printStackTrace();
            // Set defaults even if loading fails
            tfDetectionThreshold.setText("60");
            tfAutomarkThreshold.setText("80");
            tfLateThreshold.setText("15");
            cbCameraDevice.setValue(0);
            tfLogPath.setText("logs/");
        }
    }

    /**
     * Detects which camera indices are actually available
     * Runs in background to avoid blocking UI
     */
    @FXML
    private void detectCameras() {
        // Disable button during detection
        btnDetectCameras.setDisable(true);
        btnDetectCameras.setText("Detecting...");

        // Run detection in background thread
        new Thread(() -> {
            List<Integer> availableCameras = new ArrayList<>();

            // Save original stderr
            PrintStream originalErr = System.err;

            try {
                // Suppress OpenCV errors during detection
                System.setErr(new PrintStream(new java.io.OutputStream() {
                    @Override
                    public void write(int b) {
                        // Discard output
                    }
                }));

                // Test cameras 0-9
                for (int i = 0; i < 10; i++) {
                    VideoCapture camera = new VideoCapture(i);
                    if (camera.isOpened()) {
                        availableCameras.add(i);
                        camera.release();
                    }
                }

            } catch (Exception e) {
                // Restore stderr for actual errors
                System.setErr(originalErr);
                System.err.println("Error detecting cameras: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Always restore stderr
                System.setErr(originalErr);
            }

            // Update UI on JavaFX thread
            final List<Integer> cameras = availableCameras;
            Platform.runLater(() -> {
                cbCameraDevice.getItems().clear();

                if (cameras.isEmpty()) {
                    Toast.show("No cameras detected. Using default list.", Toast.ToastType.ERROR);
                    cbCameraDevice.getItems().addAll(0, 1, 2);
                } else {
                    cbCameraDevice.getItems().addAll(cameras);
                    Toast.show("Found " + cameras.size() + " camera(s)!", Toast.ToastType.SUCCESS);
                }

                // Re-enable button
                btnDetectCameras.setDisable(false);
                btnDetectCameras.setText("Detect Cameras");
            });

        }).start();
    }

    /**
     * Loads current settings from SettingsManager into the UI fields
     */
    private void loadSettings() {
        try {
            AppSettings settings = SettingsManager.getInstance().getSettings();

            tfDetectionThreshold.setText(String.valueOf(settings.getDetectionThreshold()));
            tfAutomarkThreshold.setText(String.valueOf(settings.getAutoMarkThreshold()));
            tfLateThreshold.setText(String.valueOf(settings.getLateThresholdMinutes()));
            cbCameraDevice.setValue(settings.getCameraDevice());
            tfLogPath.setText(settings.getLogPath());
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Saves the settings from UI fields back to SettingsManager
     */
    @FXML
    private void saveSettings() {
        try {
            // Validate and parse inputs
            int detectionThreshold = Integer.parseInt(tfDetectionThreshold.getText());
            int automarkThreshold = Integer.parseInt(tfAutomarkThreshold.getText());
            int lateThreshold = Integer.parseInt(tfLateThreshold.getText());
            Integer cameraDevice = cbCameraDevice.getValue();
            String logPath = tfLogPath.getText();

            // Basic validation
            if (detectionThreshold < 0 || detectionThreshold > 100) {
                Toast.show("Detection threshold must be between 0-100", Toast.ToastType.ERROR);
                return;
            }

            if (automarkThreshold < 0 || automarkThreshold > 100) {
                Toast.show("Automark threshold must be between 0-100", Toast.ToastType.ERROR);
                return;
            }

            if (automarkThreshold < detectionThreshold) {
                Toast.show("Automark threshold must more than detection threshold", Toast.ToastType.ERROR);
                return;
            }

            if (lateThreshold < 0) {
                Toast.show("Late threshold must be positive", Toast.ToastType.ERROR);
                return;
            }

            if (cameraDevice == null) {
                Toast.show("Please select a camera device", Toast.ToastType.ERROR);
                return;
            }

            if (logPath == null || logPath.trim().isEmpty()) {
                Toast.show("Log path cannot be empty", Toast.ToastType.ERROR);
                return;
            }

            // Save to SettingsManager
            AppSettings settings = SettingsManager.getInstance().getSettings();
            settings.setDetectionThreshold(detectionThreshold);
            settings.setLateThresholdMinutes(lateThreshold);
            settings.setCameraDevice(cameraDevice);
            settings.setLogPath(logPath);

            SettingsManager.getInstance().saveSettings();

            Toast.show("Settings saved successfully!", Toast.ToastType.SUCCESS);

        } catch (NumberFormatException e) {
            Toast.show("Please enter valid numbers", Toast.ToastType.ERROR);
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
            Toast.show("Error saving settings: " + e.getMessage(), Toast.ToastType.ERROR);
        }
    }


    @FXML
    public void logout() {
        AuthenticationContext.logout();
        Router.changePage(PageName.Login);
    }
}
