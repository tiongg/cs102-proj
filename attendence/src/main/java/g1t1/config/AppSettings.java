package g1t1.config;

/**
 * POJO class representing application settings.
 * These settings can be modified via the Settings UI and are persisted to JSON.
 */
public class AppSettings {
    private int detectionThreshold;
    private int autoMarkThreshold;
    private int lateThresholdMinutes;
    private int cameraDevice;
    private String logPath;

    /**
     * Constructor with default values
     */
    public AppSettings() {
        this.detectionThreshold = 20;
        this.autoMarkThreshold = 80;
        this.lateThresholdMinutes = 15;
        this.cameraDevice = 0;
        this.logPath = "logs/";
    }

    public int getDetectionThreshold() {
        return detectionThreshold;
    }

    public void setDetectionThreshold(int detectionThreshold) {
        this.detectionThreshold = detectionThreshold;
    }

    public int getAutoMarkThreshold() {
        return autoMarkThreshold;
    }

    public void setAutoMarkThreshold(int autoMarkThreshold) {
        this.autoMarkThreshold = autoMarkThreshold;
    }

    public int getLateThresholdMinutes() {
        return lateThresholdMinutes;
    }

    public void setLateThresholdMinutes(int lateThresholdMinutes) {
        this.lateThresholdMinutes = lateThresholdMinutes;
    }

    public int getCameraDevice() {
        return cameraDevice;
    }

    public void setCameraDevice(int cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
