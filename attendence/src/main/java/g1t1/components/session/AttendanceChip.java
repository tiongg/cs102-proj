package g1t1.components.session;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.models.sessions.SessionAttendance;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AttendanceChip extends HBox {
    public AttendanceChip(SessionAttendance attendance) {
        this.getChildren().add(
                new Label(attendance.getStudent().getName())
        );
        VBox statusChip = new VBox();
        Label lblStatusText = new Label();
        lblStatusText.textProperty().bind(attendance.getAttendanceProperty().map(this::getStatusText));
        statusChip.getChildren().add(lblStatusText);
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
}
