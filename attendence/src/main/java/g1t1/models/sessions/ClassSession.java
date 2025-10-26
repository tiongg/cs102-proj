package g1t1.models.sessions;

import g1t1.components.table.TableChipItem;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.models.BaseEntity;
import g1t1.models.ids.StudentID;
import g1t1.models.users.Student;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

record AttendanceStats(int present, int expected, int total) {
}

/**
 * Class session
 */
public class ClassSession extends BaseEntity implements TableChipItem {
    public static final int TIME_BEFORE_LATE = 15;

    private final ModuleSection moduleSection;
    private final int week;
    private final LocalDateTime startTime;
    private final HashMap<StudentID, SessionAttendance> studentAttendance = new HashMap<>();
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week, LocalDateTime startTime, SessionStatus status) {
        this.moduleSection = moduleSection;
        this.sessionStatus = status;
        this.startTime = startTime;
        this.week = week;
        for (Student student : moduleSection.getStudents()) {
            studentAttendance.put(student.getId(), new SessionAttendance(student));
        }
    }

    public void endSession() {
        this.sessionStatus = SessionStatus.Ended;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public int getWeek() {
        return this.week;
    }

    public HashMap<StudentID, SessionAttendance> getStudentAttendance() {
        return this.studentAttendance;
    }

    public ModuleSection getModuleSection() {
        return this.moduleSection;
    }

    /**
     * Mark students attendance as the result of this
     */
    public AttendanceStatus getCurrentStatus() {
        Duration timePassed = Duration.between(this.startTime, LocalDateTime.now());
        if (timePassed.toMinutes() <= TIME_BEFORE_LATE) {
            return AttendanceStatus.PRESENT;
        } else {
            return AttendanceStatus.LATE;
        }
    }

    private String formatModuleSection() {
        String module = moduleSection.getModule();
        String section = moduleSection.getSection();
        return String.format("%s - %s", module, section);
    }

    private AttendanceStats attendanceStats() {
        int present = 0;
        int expected = 0;
        for (SessionAttendance attendance : this.studentAttendance.values()) {
            if (attendance.getStatus() == AttendanceStatus.PENDING) {
                // ???
                continue;
            }
            // Don't count excused students
            if (attendance.getStatus() == AttendanceStatus.EXCUSED) {
                continue;
            }
            expected++;
            if (attendance.getStatus() != AttendanceStatus.LATE) {
                present++;
            }
        }
        return new AttendanceStats(present, expected, this.studentAttendance.size());
    }

    private String formatAttendance() {
        AttendanceStats stats = attendanceStats();
        return String.format("%d / %d", stats.present(), stats.expected());
    }

    private String formatRate() {
        AttendanceStats stats = attendanceStats();

        if (stats.expected() == 0) {
            return "0%";
        }
        int percent = (int) (((double) stats.present() / stats.expected()) * 100);
        return String.format("%d %s", percent, "%");
    }

    private String formatDate() {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return this.startTime.format(customFormatter);
    }

    @Override
    public String[] getChipData() {
        return new String[]{this.formatModuleSection(), this.formatDate(), this.moduleSection.getStartTime(),
                this.formatAttendance(), this.formatRate()};
    }
}
