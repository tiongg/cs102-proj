package g1t1.db.students;

import java.util.List;
import java.util.Optional;;

public interface StudentRepository {
    String create(String fullName, String email);
    List<Student> fetchAllStudents();
    Optional<Student> findById(String studentId);
    Optional<Student> findByEmail(String email);
    boolean update(String studentId, String fullNameNullable, String emailNullable);
    boolean delete(String studentId);
}
