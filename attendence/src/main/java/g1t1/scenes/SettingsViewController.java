package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.config.AppSettings;
import g1t1.config.SettingsManager;
import g1t1.models.scenes.PageController;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class SettingsViewController extends PageController {
    
    @FXML
    private TextField tfDetectionThreshold;
    
    @FXML
    private TextField tfLateThreshold;
    
    @FXML
    private ComboBox<Integer> cbCameraDevice;
    
    @FXML
    private TextField tfLogPath;
    
    @FXML
    private void initialize() {
        try {
            // Populate camera device dropdown with indices 0-5
            cbCameraDevice.getItems().addAll(0, 1, 2, 3, 4, 5);
            
            // Load current settings
            loadSettings();
        } catch (Exception e) {
            System.err.println("Error initializing Settings page: " + e.getMessage());
            e.printStackTrace();
            // Set defaults even if loading fails
            tfDetectionThreshold.setText("70");
            tfLateThreshold.setText("15");
            cbCameraDevice.setValue(0);
            tfLogPath.setText("logs/");
        }
    }
    
    /**
     * Loads current settings from SettingsManager into the UI fields
     */
    private void loadSettings() {
        try {
            AppSettings settings = SettingsManager.getInstance().getSettings();
            
            tfDetectionThreshold.setText(String.valueOf(settings.getDetectionThreshold()));
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
            int lateThreshold = Integer.parseInt(tfLateThreshold.getText());
            Integer cameraDevice = cbCameraDevice.getValue();
            String logPath = tfLogPath.getText();
            
            // Basic validation
            if (detectionThreshold < 0 || detectionThreshold > 100) {
                Toast.show("Detection threshold must be between 0-100", Toast.ToastType.ERROR);
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
}
