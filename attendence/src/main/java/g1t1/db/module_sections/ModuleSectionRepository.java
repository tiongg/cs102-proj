package g1t1.db.module_sections;

import java.util.List;
import java.util.Optional;

public interface ModuleSectionRepository {
    String create(String moduleTitle, String sectionNumber, String term, int dayOfWeek, String startTime, String endTime, String room, String teacherUserId);

    List<ModuleSectionRecord> fetchModuleSectionsByModuleTitle(String moduleTitle);

    Optional<ModuleSectionRecord> fetchModuleSectionByModuleTitleAndSectionNumberAndTerm(String moduleTitle, String sectionNumber, String term);

    List<ModuleSectionRecord> fetchModuleSectionsByTeacherUserId(String teacherUserId);

    boolean update(String moduleSectionId, String moduleTitleNullable, String sectionNumberNullable, String termNullable, String startTimeNullable, String endTimeNullable, String roomNullable, String teacherUserIdNullable);

    boolean delete(String moduleSectionId);
}
