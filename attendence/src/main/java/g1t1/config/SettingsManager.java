package g1t1.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Singleton manager for application settings.
 * Handles loading settings from JSON file and saving changes back.
 */
public class SettingsManager {
    private static SettingsManager instance;
    private static final String SETTINGS_FILE = "settings.json";
    private AppSettings settings;
    private final Gson gson;

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
            System.out.println("Settings saved to: " + SETTINGS_FILE);
        } catch (IOException e) {
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

    public int getLateThresholdMinutes() {
        return settings.getLateThresholdMinutes();
    }

    public int getCameraDevice() {
        return settings.getCameraDevice();
    }

    public String getLogPath() {
        return settings.getLogPath();
    }

    // Convenience setters that auto-save
    public void setDetectionThreshold(int threshold) {
        settings.setDetectionThreshold(threshold);
        saveSettings();
    }

    public void setLateThresholdMinutes(int minutes) {
        settings.setLateThresholdMinutes(minutes);
        saveSettings();
    }

    public void setCameraDevice(int device) {
        settings.setCameraDevice(device);
        saveSettings();
    }

    public void setLogPath(String path) {
        settings.setLogPath(path);
        saveSettings();
    }
}
