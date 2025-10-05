package g1t1.db.enrollments;

public record Enrollment(
    String enrollmentId,
    String moduleSectionId,
    String studentId
) {}