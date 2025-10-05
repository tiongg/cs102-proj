package g1t1.db.enrollments;

import java.util.Optional;
import java.util.List;

public interface EnrollmentRepository {
    String create(String moduleSectionId, String studentId);
    Optional<Enrollment> fetchEnrollmentById(String enrollmentId);
    List<Enrollment> fetchEnrollmentsByModuleSectionId(String moduleSectionId);
    List<Enrollment> fetchEnrollmentsByStudentId(String studentId);
    boolean update(String enrollmentId, String moduleSectionIdNullable, String studentIdNullable);
    boolean deleteById(String enrollmentId);
    boolean deleteByModuleSectionId(String moduleSectionId);
    boolean deleteByStudentId(String studentId);
}
