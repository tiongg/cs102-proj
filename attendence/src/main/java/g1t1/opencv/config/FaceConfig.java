package g1t1.opencv.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for face recognition system.
 * Loads configuration from properties file with fallback defaults.
 */
public class FaceConfig {
    private static FaceConfig instance;
    private Properties properties;

    private FaceConfig() {
        loadProperties();
    }

    public static FaceConfig getInstance() {
        if (instance == null) {
            instance = new FaceConfig();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("face-recognition.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not load face-recognition.properties, using defaults");
        }
    }

    // Recognition Settings
    public double getRecognitionThreshold() {
        return Double.parseDouble(properties.getProperty("recognition.threshold", "70.0"));
    }

    public double getDisplayThreshold() {
        return Double.parseDouble(properties.getProperty("recognition.display.threshold", "40.0"));
    }

    // Detection Settings
    public double getScaleFactor() {
        return Double.parseDouble(properties.getProperty("detection.scale.factor", "1.1"));
    }

    public int getMinNeighbors() {
        return Integer.parseInt(properties.getProperty("detection.min.neighbors", "3"));
    }

    public int getMinSize() {
        return Integer.parseInt(properties.getProperty("detection.min.size", "30"));
    }

    // Camera Settings
    public int getCameraIndex() {
        return Integer.parseInt(properties.getProperty("camera.index", "0"));
    }

    public int getTargetFps() {
        return Integer.parseInt(properties.getProperty("target.fps", "15"));
    }

    public int getCameraWidth() {
        return Integer.parseInt(properties.getProperty("camera.width", "640"));
    }

    public int getCameraHeight() {
        return Integer.parseInt(properties.getProperty("camera.height", "480"));
    }

    // Advanced Features
    public boolean isLivenessEnabled() {
        return Boolean.parseBoolean(properties.getProperty("liveness.enabled", "true"));
    }

    public int getBlinkThreshold() {
        return Integer.parseInt(properties.getProperty("liveness.blink.threshold", "3"));
    }

    public boolean isMaskDetectionEnabled() {
        return Boolean.parseBoolean(properties.getProperty("mask.detection.enabled", "true"));
    }

    public boolean isLoggingEnabled() {
        return Boolean.parseBoolean(properties.getProperty("logging.enabled", "true"));
    }

    // Runtime configuration updates
    public void setRecognitionThreshold(double threshold) {
        properties.setProperty("recognition.threshold", String.valueOf(threshold));
    }

    public void setDisplayThreshold(double threshold) {
        properties.setProperty("recognition.display.threshold", String.valueOf(threshold));
    }
}