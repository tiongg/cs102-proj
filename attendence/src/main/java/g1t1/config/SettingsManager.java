package g1t1.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.videoio.VideoCapture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import g1t1.components.Toast;
import g1t1.features.logger.AppLogger;
import g1t1.features.logger.LogLevel;
import javafx.application.Platform;

/**
 * Singleton manager for application settings. Handles loading settings from
 * JSON file and saving changes back.
 */
public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.json";
    private static SettingsManager instance;
    private final Gson gson;
    private AppSettings settings;

    private SettingsManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    /**
     * Loads settings from JSON file. If file doesn't exist, creates with defaults.
     */
    private void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);

        if (!settingsFile.exists()) {
            // Create default settings file
            this.settings = new AppSettings();
            try {
                saveSettings();
                System.out.println("Created default settings file at: " + SETTINGS_FILE);
            } catch (Exception e) {
                System.err.println("Could not create settings file, using defaults in memory only");
            }
        } else {
            // Load existing settings
            try (FileReader reader = new FileReader(settingsFile)) {
                this.settings = gson.fromJson(reader, AppSettings.class);
                if (this.settings == null) {
                    this.settings = new AppSettings();
                }
                System.out.println("Loaded settings from: " + SETTINGS_FILE);
            } catch (Exception e) {
                System.err.println("Error loading settings, using defaults: " + e.getMessage());
                this.settings = new AppSettings();
            }
        }
    }

    /**
     * Saves current settings to JSON file.
     */
    public void saveSettings() {
        try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(this.settings, writer);
            AppLogger.logf("Settings saved to: %s", new File(SETTINGS_FILE).getAbsolutePath());
            System.out.println("Settings saved to: " + SETTINGS_FILE);
        } catch (IOException e) {
            AppLogger.logf(LogLevel.Error, "Error saving settings: %s", e.getMessage());
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    /**
     * Gets the current settings object.
     */
    public AppSettings getSettings() {
        return settings;
    }

    // Convenience getters for individual settings
    public int getDetectionThreshold() {
        return settings.getDetectionThreshold();
    }

    // Convenience setters that auto-save
    public void setDetectionThreshold(int threshold) {
        int oldValue = settings.getDetectionThreshold();
        settings.setDetectionThreshold(threshold);
        AppLogger.logf("Detection threshold changed: %d -> %d", oldValue, threshold);
        saveSettings();
    }

    public int getLateThresholdMinutes() {
        return settings.getLateThresholdMinutes();
    }

    public void setLateThresholdMinutes(int minutes) {
        int oldValue = settings.getLateThresholdMinutes();
        settings.setLateThresholdMinutes(minutes);
        AppLogger.logf("Late threshold changed: %d -> %d minutes", oldValue, minutes);
        saveSettings();
    }

    public int getCameraDevice() {
        return settings.getCameraDevice();
    }

    public void setCameraDevice(int device) {
        int oldValue = settings.getCameraDevice();
        settings.setCameraDevice(device);
        AppLogger.logf("Camera device changed: %d -> %d", oldValue, device);
        saveSettings();
    }

    public String getLogPath() {
        return settings.getLogPath();
    }

    public void setLogPath(String path) {
        String oldValue = settings.getLogPath();
        settings.setLogPath(path);
        AppLogger.logf("Log path changed: %s -> %s", oldValue, path);
        saveSettings();
    }

    public VideoCapture getConfiguredCamera() {
        int configuredDevice = getCameraDevice();
        AppLogger.logf("Attempting to open camera device: %d", configuredDevice);
        VideoCapture camera = new VideoCapture(configuredDevice);

        // Check if configured camera opened successfully
        if (!camera.isOpened()) {
            AppLogger.logf(LogLevel.Warning, "Camera %d failed to open, trying camera 0...", configuredDevice);
            System.err.println("Camera " + configuredDevice + " failed to open. Trying camera 0...");
            camera.release();
            camera = new VideoCapture(0);

            // If camera 0 also fails, notify user
            if (!camera.isOpened()) {
                AppLogger.log(LogLevel.Error, "No cameras available!");
                System.err.println("No cameras available!");
                Platform.runLater(() -> {
                    Toast.show("No camera detected!", Toast.ToastType.ERROR);
                });
            } else {
                AppLogger.logf("Successfully opened default camera 0 (Camera %d was unavailable)", configuredDevice);
                Platform.runLater(() -> {
                    Toast.show("Using default camera (Camera " + configuredDevice + " unavailable)",
                            Toast.ToastType.WARNING);
                });
            }
        } else {
            AppLogger.logf("Camera device %d opened successfully", configuredDevice);
        }
        return camera;
    }
}
