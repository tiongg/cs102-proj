package g1t1.models.sessions;

import g1t1.components.table.TableChipItem;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.models.BaseEntity;
import g1t1.models.users.Student;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class session
 */
public class ClassSession extends BaseEntity implements TableChipItem {
    private final ModuleSection moduleSection;
    private final int week;
    private final LocalDateTime startTime;
    private final ArrayList<SessionAttendance> studentAttendance = new ArrayList<>();
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week, LocalDateTime startTime, SessionStatus status) {
        this.moduleSection = moduleSection;
        this.sessionStatus = status;
        this.startTime = startTime;
        this.week = week;
        for (Student student : moduleSection.getStudents()) {
            studentAttendance.add(new SessionAttendance(student));
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

    public List<SessionAttendance> getStudentAttendance() {
        return this.studentAttendance;
    }

    public ModuleSection getModuleSection() {
        return this.moduleSection;
    }

    private String formatModuleSection() {
        String module = moduleSection.getModule();
        String section = moduleSection.getSection();
        return String.format("%s - %s", module, section);
    }

    private String formatAttendance() {
        int present = 0;
        int expected = 0;
        for (SessionAttendance attendance : this.studentAttendance) {
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

        return String.format("%d / %d", present, expected);
    }

    private String formatRate() {
        int present = 0;
        int expected = 0;
        for (SessionAttendance attendance : this.studentAttendance) {
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
        if (expected == 0) {
            return "0%";
        }
        int percent = (int) (((double) present / expected) * 100);
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
