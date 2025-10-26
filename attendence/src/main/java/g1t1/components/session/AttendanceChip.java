package g1t1.components.session;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.models.sessions.SessionAttendance;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class AttendanceChip extends HBox {
    public AttendanceChip(SessionAttendance attendance) {
        // Configure the HBox container with better contrast
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(8);
        this.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8px;"); // White background
        this.setPadding(new Insets(8, 12, 8, 12));
        this.setMaxWidth(Double.MAX_VALUE);
        this.setPrefWidth(Double.MAX_VALUE);

        // Student name label with dark text
        Label lblStudentName = new Label(attendance.getStudent().getName());
        lblStudentName.setAlignment(Pos.CENTER);
        lblStudentName.setMaxWidth(Double.MAX_VALUE);
        lblStudentName.setStyle("-fx-font-size: 14px; -fx-text-fill: #2C2C2C;"); // Dark gray text
        HBox.setHgrow(lblStudentName, Priority.ALWAYS);

        // Status chip
        Label lblStatus = new Label();
        lblStatus.textProperty().bind(attendance.getAttendanceProperty().map(this::getStatusText));
        lblStatus.setAlignment(Pos.CENTER);
        lblStatus.setPadding(new Insets(4, 12, 4, 12));

        // Bind style based on status
        attendance.getAttendanceProperty().addListener((obs, oldVal, newVal) -> {
            lblStatus.setStyle(getStatusStyle(newVal));
        });
        lblStatus.setStyle(getStatusStyle(attendance.getAttendanceProperty().get()));

        this.getChildren().addAll(lblStudentName, lblStatus);
    }

    private String getStatusText(AttendanceStatus status) {
        return switch (status) {
            case LATE -> "Late";
            case ABSENT -> "Absent";
            case EXCUSED -> "Excused";
            case PENDING -> "Pending";
            case PRESENT -> "Present";
        };
    }

    private String getStatusStyle(AttendanceStatus status) {
        String baseStyle = "-fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;";
        return switch (status) {
            case PRESENT -> baseStyle + " -fx-background-color: #4CAF50; -fx-text-fill: white;";
            case LATE -> baseStyle + " -fx-background-color: #FF6B6B; -fx-text-fill: white;";
            case ABSENT -> baseStyle + " -fx-background-color: #FF6B6B; -fx-text-fill: white;";
            case EXCUSED -> baseStyle + " -fx-background-color: #64B5F6; -fx-text-fill: white;";
            case PENDING -> baseStyle + " -fx-background-color: #FFA726; -fx-text-fill: white;";
        };
    }
}