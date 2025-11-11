package g1t1.models.sessions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import g1t1.components.table.TableChipItem;
import g1t1.db.module_sections.ModuleSectionRecord;
import g1t1.models.users.Student;
import g1t1.utils.DateUtils;

/**
 * Module & Section. Used to uniquely identify a class.
 */
public class ModuleSection implements TableChipItem {
    /**
     * Module of the class. i.e CS102
     */
    private final String module;
    /**
     * Section of the class. i.e G1
     */
    private final String section;
    /**
     * Term the class takes place in. i.e AY25-26T1
     */
    private final String term;
    /**
     * Students enrolled in class
     */
    private final List<Student> students = new ArrayList<>();
    /**
     * Day of Week of class
     */
    private final int day;
    /**
     * Where the class is held
     */
    private final String room;
    /**
     * Start time of lesson (HH:MM)
     */
    private final String startTime;
    /**
     * End time of lesson (HH:MM)
     */
    private final String endTime;
    /**
     * Module section id. Used mainly for db stuff
     */
    private String id;

    public ModuleSection(String module, String section, String term, String room, int day, String startTime,
            String endTime) {
        this.module = module;
        this.section = section;
        this.term = term;
        this.room = room;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.id = module + " - " + section;
    }

    public ModuleSection(String id, String module, String section, String term, String room, int day, String startTime,
            String endTime) {
        this(module, section, term, room, day, startTime, endTime);
        this.id = id;
    }

    public ModuleSection(ModuleSectionRecord dbModuleSection) {
        this.module = dbModuleSection.moduleTitle();
        this.section = dbModuleSection.sectionNumber();
        this.term = dbModuleSection.term();
        this.room = dbModuleSection.room();
        this.day = dbModuleSection.dayOfWeek();
        this.startTime = dbModuleSection.startTime();
        this.endTime = dbModuleSection.endTime();
        this.id = dbModuleSection.moduleSectionId();
    }

    /**
     * Gets current acad year and term in the format of: AYy1-y2T<term>
     */
    public static String getCurrentAYTerm() {
        int currentMonth = LocalDateTime.now().getMonthValue();
        int currentYear = LocalDateTime.now().getYear();

        // After aug. Aug - Dec is T1 of AY currentYear, currentYear + 1
        if (currentMonth >= 8) {
            return String.format("AY%d-%02d T1", currentYear, (currentYear + 1) % 100);
        }
        // Jan - May (Term 2) of AY currentYear - 1, currentYear
        if (currentMonth < 5) {
            return String.format("AY%d-%02d T2", currentYear - 1, (currentYear) % 100);
        }
        // Term 3, special term
        else {
            return String.format("AY%d-%02d T3", currentYear - 1, (currentYear) % 100);
        }
    }

    public String getId() {
        return id;
    }

    /**
     * Module of the class. i.e CS102
     */
    public String getModule() {
        return module;
    }

    /**
     * Section of the class. i.e G1
     */
    public String getSection() {
        return section;
    }

    public String getTerm() {
        return term;
    }

    public int getDay() {
        return day;
    }

    public String getRoom() {
        return room;
    }

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public List<Student> getStudents() {
        return this.students;
    }

    public List<Student> getActiveStudents() {
        return this.students.stream().filter(Student::getIsActive).toList();
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    @Override
    public String toString() {
        return "ModuleSection [module=" + module + ", section=" + section + "]";
    }

    private String formatClassDuration() {
        return String.format("%s - %s", this.startTime, this.endTime);
    }

    @Override
    public String[] getChipData() {
        // "Module", "Section", "Day", "Time", "Enrolled"
        return new String[] { this.module, this.section, DateUtils.dayOfWeekToWord(this.getDay()),
                formatClassDuration(), Integer.toString(this.students.size()) };
    }

    @Override
    public long[] getComparatorKeys() {
        String[] startTimeRaw = this.startTime.split(":");
        int hours = Integer.parseInt(startTimeRaw[0]);
        int minutes = Integer.parseInt(startTimeRaw[1]);
        int totalMinutes = hours * 60 + minutes;
        return new long[] { this.module.hashCode(), this.section.hashCode(), this.getDay(), totalMinutes,
                this.students.size() };
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ModuleSection otherSection) {
            return this.id.equals(otherSection.getId());
        }
        return false;
    }

}
