package g1t1.config;

/**
 * POJO class representing application settings.
 * These settings can be modified via the Settings UI and are persisted to JSON.
 */
public class AppSettings {
    private int detectionThreshold;
    private int lateThresholdMinutes;
    private int cameraDevice;
    private String logPath;

    /**
     * Constructor with default values
     */
    public AppSettings() {
        this.detectionThreshold = 70;
        this.lateThresholdMinutes = 15;
        this.cameraDevice = 0;
        this.logPath = "logs/";
    }

    /**
     * Constructor with all parameters
     */
    public AppSettings(int detectionThreshold, int lateThresholdMinutes, int cameraDevice, String logPath) {
        this.detectionThreshold = detectionThreshold;
        this.lateThresholdMinutes = lateThresholdMinutes;
        this.cameraDevice = cameraDevice;
        this.logPath = logPath;
    }

    // Getters
    public int getDetectionThreshold() {
        return detectionThreshold;
    }

    public int getLateThresholdMinutes() {
        return lateThresholdMinutes;
    }

    public int getCameraDevice() {
        return cameraDevice;
    }

    public String getLogPath() {
        return logPath;
    }

    // Setters
    public void setDetectionThreshold(int detectionThreshold) {
        this.detectionThreshold = detectionThreshold;
    }

    public void setLateThresholdMinutes(int lateThresholdMinutes) {
        this.lateThresholdMinutes = lateThresholdMinutes;
    }

    public void setCameraDevice(int cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
