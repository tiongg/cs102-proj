package g1t1.db.sessions;

import java.sql.Date;
import java.sql.Timestamp;

public record SessionRecord(
        String sessionId,
        String moduleSectionId,
        Date date,
        short week,
        Timestamp startTime,
        Timestamp endTime,
        String status,
        Timestamp createdAt
) {
}
