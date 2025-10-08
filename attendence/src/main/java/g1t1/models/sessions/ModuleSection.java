package g1t1.models.sessions;

import g1t1.components.table.TableChipItem;
import g1t1.db.module_sections.ModuleSectionRecord;
import g1t1.models.users.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Module & Section. Used to uniquely identify a class.
 */
public class ModuleSection implements TableChipItem {
    /**
     * Module section id. Used mainly for db stuff
     */
    private final String id;
    /**
     * Module of the class. i.e CS102
     */
    private final String module;
    /**
     * Section of the class. i.e G1
     */
    private final String section;
    /**
     * Students enrolled in class
     */
    private final List<Student> students = new ArrayList<>();
    /**
     * Day of Week of class
     */
    private final int day;
    /**
     * Start time of lesson (HH:MM)
     */
    private final String startTime;
    /**
     * End time of lesson (HH:MM)
     */
    private final String endTime;

    public ModuleSection(String module, String section, int day, String startTime, String endTime) {
        this.module = module;
        this.section = section;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.id = module + " - " + section;
    }

    public ModuleSection(ModuleSectionRecord dbModuleSection) {
        this.module = dbModuleSection.moduleTitle();
        this.section = dbModuleSection.sectionNumber();
        this.day = dbModuleSection.dayOfWeek();
        this.startTime = dbModuleSection.startTime();
        this.endTime = dbModuleSection.endTime();
        this.id = dbModuleSection.moduleSectionId();
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

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public List<Student> getStudents() {
        return this.students;
    }

    public String getStartTime() {
        return this.startTime;
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
        return new String[]{this.module, this.section, Integer.toString(this.day), formatClassDuration(),
                Integer.toString(this.students.size())};
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ModuleSection) {
            ModuleSection otherSection = (ModuleSection) other;
            if (this.getSection().equals(otherSection.getSection())
                    && this.getModule().equals(otherSection.getModule())) {
                return true;
            }
        }
        return false;
    }
}
