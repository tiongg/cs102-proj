# Face Recognition System

## Requirements Met

✅ **Core Functionality**
- `start(List<Student>)` - Initiates face recognition with enrolled students
- `stop()` - Terminates recognition session
- **Event-driven architecture** - Emits `StudentDetectedEvent` with ID, name, confidence
- **AppLogger integration** - Logs each student detection once (section, name, ID)
- **≥8 FPS processing** - Real-time frame processing with performance optimizations

✅ **Face Detection & Recognition**
- **Live webcam feed** with HaarCascade face detection
- **Preprocessing pipeline** - Grayscale → Normalize → Histogram Equalization → Resize
- **Multiple algorithms** - Histogram matching, mask-aware recognition
- **Visual feedback** - Bounding boxes (Green=Detection, Blue=Recognition, Red=Photo/Spoof)
- **Configurable thresholds** - 70% recognition, 40% display
- **Liveness detection** - Photo spoofing prevention

✅ **Clean Architecture**
- **OOP Design Patterns** - Strategy (preprocessing), Template Method (recognition), Singleton (service)
- **SOLID principles** - Single responsibility, dependency injection, polymorphic recognizers
- **Configuration externalization** - Properties file with runtime updates
- **Performance optimizations** - Pre-computed histograms, recognition caching, frame optimization

---

## Frontend Integration

### Quick Start
```java
// 1. Get service instance
FaceRecognitionService service = FaceRecognitionService.getInstance();

// 2. Setup event listener
service.getEventEmitter().addListener(new FaceEventListener() {
    @Override
    public void onStudentDetected(StudentDetectedEvent event) {
        Platform.runLater(() -> {
            updateAttendanceUI(event.getStudent(), event.getConfidence());
        });
    }
});

// 3. Start recognition
List<Student> enrolledStudents = getStudentsForClass();
service.start(enrolledStudents);

// 4. Stop when done
service.stop();
```

### Event System Integration

The face recognition system uses an **event-driven architecture** to communicate with the frontend. This design pattern decouples the OpenCV processing from the UI, enabling real-time updates without blocking the camera feed or requiring constant polling.

**How It Works:**
1. **Face Recognition Service** processes camera frames at ≥8 FPS
2. When a student is recognized above the confidence threshold, a **StudentDetectedEvent** is emitted
3. **Frontend listeners** receive these events and update the UI accordingly
4. **No polling required** - the frontend reacts to events as they happen

**StudentDetectedEvent Properties:**
- `getStudent()` - Complete Student object with ID, name, section information
- `getConfidence()` - Recognition confidence percentage (0-100%)
- **Smart Updates**: Events are only emitted for new students or when confidence improves by ≥1%
- **Session Tracking**: Automatically maintains the maximum confidence achieved per student

**Event Handling Best Practices:**
```java
@Override
public void onStudentDetected(StudentDetectedEvent event) {
    // CRITICAL: Always wrap UI updates in Platform.runLater()
    // OpenCV runs on background thread, JavaFX requires UI thread
    Platform.runLater(() -> {
        String studentInfo = String.format("%s (%.1f%%)",
            event.getStudent().getName(), event.getConfidence());
        attendanceList.getItems().add(studentInfo);

        // Update attendance counter
        updateAttendanceCount(attendanceList.getItems().size());

        // Optional: Play notification sound
        playDetectionSound();
    });
}
```

**Additional Event Types:**
The system also emits `AttendanceSessionEvent` for session lifecycle management:
- `SESSION_STARTED` - When face recognition begins
- `SESSION_ENDED` - When face recognition stops
- `STUDENT_UPDATED` - When student confidence improves

### Service Lifecycle Management

The `FaceRecognitionService` follows a **singleton pattern** with clear lifecycle states to prevent resource conflicts and ensure proper camera management.

**Service States:**
- **Stopped** - Initial state, no resources allocated
- **Running** - Camera active, processing frames, emitting events
- **Stopping** - Cleanup in progress, releasing resources

**Lifecycle Best Practices:**

```java
// ALWAYS check service state before operations
FaceRecognitionService service = FaceRecognitionService.getInstance();

if (service.isRunning()) {
    // Handle already running scenario - either stop first or show warning
    showWarningDialog("Face recognition already active in another session");
    return;
}

// Safe to start new session
try {
    List<Student> enrolledStudents = getStudentsForCurrentClass();
    service.start(enrolledStudents);

    // Update UI to show active state
    startButton.setText("Stop Recognition");
    statusLabel.setText("Face Recognition Active");

} catch (Exception e) {
    showErrorDialog("Failed to start camera: " + e.getMessage());
}
```

**Resource Management:**
```java
// Get enrollment info for UI display
int enrolledCount = service.getEnrolledStudentCount();
studentCountLabel.setText(enrolledCount + " students enrolled");

// Get current session data for attendance tracking
AttendanceSession session = service.getCurrentSession();
if (session != null) {
    int detectedCount = session.getDetectedStudents().size();
    progressBar.setProgress(detectedCount / (double) enrolledCount);
}

// CRITICAL: Always stop service when closing application
@Override
public void stop() throws Exception {
    if (service.isRunning()) {
        service.stop(); // Releases camera and cleanup resources
    }
    super.stop();
}
```

**Thread Safety Considerations:**
- Service uses **background threads** for camera processing
- **Main thread** is never blocked by OpenCV operations
- **UI updates** must use `Platform.runLater()` from event handlers
- **Service state changes** are thread-safe and atomic

### Error Handling
```java
try {
    service.start(students);
} catch (Exception e) {
    Platform.runLater(() ->
        showErrorDialog("Camera access failed: " + e.getMessage())
    );
}
```

---

## Settings Configuration

### Configuration File: `face-recognition.properties`

**Recognition Settings**
- `recognition.threshold=70.0` - Confidence threshold for logging detection
- `recognition.display.threshold=40.0` - Confidence threshold for visual display

**Camera Settings**
- `camera.index=0` - Camera device index (change for external camera)
- `target.fps=12` - Target frame processing rate

**Feature Toggles**
- `mask.detection.enabled=true` - Enable mask-aware recognition
- `liveness.enabled=false` - Enable photo spoofing detection
- `logging.enabled=true` - Enable detection logging

### Runtime Configuration Updates
```java
FaceConfig config = FaceConfig.getInstance();
config.setRecognitionThreshold(75.0);  // Update threshold dynamically
```

---

## Testing

From VSCode terminal in root directory (`cs102-proj`):

### System Validation
**From root directory (`cs102-proj`) - PowerShell:**
```powershell
cd attendence; mvn compile exec:java -Dexec.mainClass=g1t1.opencv.testing.SystemValidationTest
```
**From attendence directory - PowerShell:**
```powershell
mvn compile exec:java "-Dexec.mainClass=g1t1.opencv.testing.SystemValidationTest"
```
**Validates:** Service lifecycle, event system, basic functionality

### Frontend Integration Examples
**From attendence directory - PowerShell:**
```powershell
mvn compile exec:java "-Dexec.mainClass=g1t1.opencv.testing.FrontendIntegrationExample"
```
**Demonstrates:** JavaFX integration patterns, event handling, configuration management, error handling

### Live JavaFX Frontend Demo
**From attendence directory - PowerShell:**
```powershell
mvn compile exec:java "-Dexec.mainClass=g1t1.opencv.testing.MinimalFrontendDemo"
```
**Shows:** JavaFX UI integration with live camera window and attendance tracking

**What you should see:**
- JavaFX control window with Start/Stop buttons and attendance list
- When you click "Start Recognition" → separate OpenCV camera window opens
- Real-time face detection with bounding boxes and recognition results

### Test Requirements
1. **Camera access** - Ensure camera is available and not in use
2. **Test photos** - Add student photos to `test-photos/[name]/` folders
3. **Face positioning** - Position face clearly in camera view during tests

## Troubleshooting

### Camera Issues
**Problem:** JavaFX window opens but no camera window appears when clicking "Start Recognition"

**Causes & Solutions:**
1. **Camera in use by another app**
   - Close any video calling apps (Teams, Zoom, Skype, etc.)
   - Close Windows Camera app if open
   - Restart computer if camera remains stuck

2. **Camera permissions denied**
   - Go to Windows Settings → Privacy → Camera
   - Enable "Allow apps to access your camera"
   - Allow Java/Maven to access camera

3. **Wrong camera index**
   - Try external USB camera if built-in fails
   - Default uses camera index 0, may need to change to 1

4. **OpenCV camera errors** (like `Error: -1072875772`)
   - This indicates Windows Media Foundation camera access issues
   - Try running VSCode as Administrator
   - Restart Windows Camera service in Services.msc

### Other Issues
- **No students loaded**: Add photos to `test-photos/[name]/` folders (JPEG format)
- **Face not detected**: Ensure good lighting and face clearly visible in frame

---

## Architecture Overview

**Core Components:**
- `FaceRecognitionService` - Main service facade
- `FaceDetector` - OpenCV face detection
- `HistogramRecognizer` / `MaskAwareRecognizer` - Recognition algorithms
- `EventEmitter` - Event-driven communication
- `FaceConfig` - Configuration management

**Data Flow:**
1. Frontend calls `start(List<Student>)`
2. Service processes camera frames at ≥8 FPS
3. Faces detected → Recognition attempted → Events emitted
4. Frontend receives `StudentDetectedEvent` updates
5. AppLogger records detections once per student
6. Frontend calls `stop()` to end session