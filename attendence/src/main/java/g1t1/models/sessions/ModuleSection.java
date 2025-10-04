package g1t1.models.sessions;

import g1t1.models.users.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Module & Section. Used to uniquely identify a class.
 */
public class ModuleSection {
    /**
     * Module of the class. i.e CS102
     */
    private final String module;

    /**
     * Section of the class. i.e G1
     */
    private final String section;

    private final List<Student> students = new ArrayList<>();

    public ModuleSection(String module, String section) {
        this.module = module;
        this.section = section;
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
}
