package g1t1.db.attendance;

import java.time.LocalDateTime;

public record Attendance(
    String sessionId,
    String enrollmentId,
    String status,
    LocalDateTime recordedTimestamp
) {}
