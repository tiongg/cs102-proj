package g1t1.db.attendance;

import java.time.LocalDateTime;

public record Attendance(
        String sessionId,
        String enrollmentId,
        String status,
        double confidence,
        String method,
        LocalDateTime recordedTimestamp
) {
}
