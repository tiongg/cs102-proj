package g1t1.db.module_sections;

public record ModuleSectionRecord(
        String moduleSectionId,
        String moduleTitle,
        String sectionNumber,
        String term,
        int dayOfWeek,
        String startTime,
        String endTime,
        String room,
        String teacherUserId
) {
}