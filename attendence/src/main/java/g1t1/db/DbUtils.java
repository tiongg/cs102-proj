package g1t1.db;

import g1t1.db.module_sections.ModuleSectionRepository;
import g1t1.db.module_sections.ModuleSectionRepositoryJooq;
import g1t1.db.student_face_images.StudentFaceImage;
import g1t1.db.student_face_images.StudentFaceImageRepository;
import g1t1.db.student_face_images.StudentFaceImageRepositoryJooq;
import g1t1.db.students.StudentRecord;
import g1t1.db.students.StudentRepository;
import g1t1.db.students.StudentRepositoryJooq;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Student;
import org.jooq.exception.DataAccessException;

import java.sql.SQLException;
import java.util.List;

public class DbUtils {
    public static Student getStudentById(String id, ModuleSection moduleSection) throws SQLException, DataAccessException {
        try (DSLInstance dslInstance = new DSLInstance()) {
            StudentRepository studentRepository = new StudentRepositoryJooq(dslInstance.dsl);
            StudentFaceImageRepository faceImageRepository = new StudentFaceImageRepositoryJooq(dslInstance.dsl);

            StudentRecord dbStudent = studentRepository.fetchStudentById(id).orElse(null);
            if (dbStudent == null) {
                return null;
            }
            List<StudentFaceImage> dbImages = faceImageRepository.fetchFaceImagesByStudentId(id);
            return new Student(dbStudent, dbImages, moduleSection);
        }
    }

    public static String saveModuleSection(ModuleSection moduleSection, String userId) throws SQLException, DataAccessException {
        try (DSLInstance dslInstance = new DSLInstance()) {
            ModuleSectionRepository moduleSectionRepository = new ModuleSectionRepositoryJooq(dslInstance.dsl);
            return moduleSectionRepository.create(
                    moduleSection.getModule(),
                    moduleSection.getSection(),
                    moduleSection.getTerm(),
                    moduleSection.getDay(),
                    moduleSection.getStartTime(),
                    moduleSection.getEndTime(),
                    moduleSection.getRoom(),
                    userId
            );
        }
    }
}
