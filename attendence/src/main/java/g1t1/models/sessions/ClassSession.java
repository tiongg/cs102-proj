package g1t1.models.sessions;

import java.util.ArrayList;
import java.util.Date;

import g1t1.models.BaseEntity;
import g1t1.models.users.Student;
import g1t1.models.users.Teacher;

enum SessionStatus {
    /**
     * Newly created session. Start session first!
     */
    Invalid,
    /**
     * Active session. End session to save.
     */
    Active,
    /**
     * Ended session. Ready to be saved.
     */
    Ended
}

/**
 * Class session
 */
public class ClassSession extends BaseEntity {
    private ModuleSection moduleSection;
    private Teacher teacher;
    private ArrayList<Student> students;
    private Date startTime;
    private SessionStatus sessionStatus;
}
