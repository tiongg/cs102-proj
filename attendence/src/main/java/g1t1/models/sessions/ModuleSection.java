package g1t1.models.sessions;

import java.util.ArrayList;
import java.util.List;

import g1t1.components.table.TableChipItem;
import g1t1.models.users.Student;

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
     * Day of Week of class
     */
    private int day;

    /**
     * Time of lesson (HH:MM)
     */
    private String time;

    /**
     * Students enrolled in class
     */
    private final List<Student> students = new ArrayList<>();

    public ModuleSection(String module, String section, int day, String time) {
        this.module = module;
        this.section = section;
        this.day = day;
        this.time = time;
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

    @Override
    public String toString() {
        return "ModuleSection [module=" + module + ", section=" + section + "]";
    }

    @Override
    public String[] getChipData() {
        // "Module", "Section", "Day", "Time", "Enrolled"
        return new String[] { this.module, this.section, Integer.toString(this.day), this.time,
                Integer.toString(this.students.size()) };
    }
}
