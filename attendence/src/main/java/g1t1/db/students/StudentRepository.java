package g1t1.db.students;

import java.util.List;
import java.util.Optional;

;

public interface StudentRepository {
    String create(String studentId, String fullName, String email);

    List<StudentRecord> fetchAllStudents();

    Optional<StudentRecord> fetchStudentById(String studentId);

    Optional<StudentRecord> fetchStudentByEmail(String email);

    boolean update(String studentId, String fullNameNullable, String emailNullable);

    boolean delete(String studentId);

    boolean setStudentActive(String studentId, boolean isActive);
}
