package g1t1.models.sessions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import g1t1.components.table.TableChipItem;
import g1t1.config.SettingsManager;
import g1t1.db.attendance.AttendanceRecord;
import g1t1.db.attendance.AttendanceStatus;
import g1t1.db.attendance.MarkingMethod;
import g1t1.db.sessions.SessionRecord;
import g1t1.models.BaseEntity;
import g1t1.models.ids.StudentID;
import g1t1.models.users.Student;
import g1t1.utils.DateUtils;

/**
 * Class session
 */
public class ClassSession extends BaseEntity implements TableChipItem {
    private final ModuleSection moduleSection;
    private final int week;
    private final LocalDateTime startTime;
    private final HashMap<StudentID, SessionAttendance> studentAttendance = new HashMap<>();
    private LocalDateTime endTime;
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week, LocalDateTime startTime, SessionStatus status) {
        this.moduleSection = moduleSection;
        this.sessionStatus = status;
        this.startTime = startTime;
        this.week = week;
        for (Student student : moduleSection.getActiveStudents()) {
            studentAttendance.put(student.getId(), new SessionAttendance(student));
        }
    }

    public ClassSession(SessionRecord sessionRecord, ModuleSection moduleSection,
            List<AttendanceRecord> attendanceRecords, HashMap<String, Student> enrollmentToStudents) {
        this.moduleSection = moduleSection;
        this.sessionStatus = SessionStatus.valueOfLabel(sessionRecord.status());
        this.week = sessionRecord.week();
        this.startTime = DateUtils.timestampToLocalDateTime(sessionRecord.startTime());
        this.endTime = DateUtils.timestampToLocalDateTime(sessionRecord.endTime());
        for (AttendanceRecord record : attendanceRecords) {
            Student student = enrollmentToStudents.get(record.enrollmentId());
            studentAttendance.put(student.getId(), new SessionAttendance(student, record));
        }
    }

    public void endSession() {
        this.sessionStatus = SessionStatus.Ended;
        this.endTime = LocalDateTime.now();
        for (SessionAttendance attendance : this.studentAttendance.values()) {
            if (attendance.getStatus() == AttendanceStatus.PENDING) {
                attendance.setStatus(AttendanceStatus.ABSENT, 1, MarkingMethod.MANUAL);
            }
        }
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
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

    public SessionStatus getSessionStatus() {
        return this.sessionStatus;
    }

    /**
     * Mark students attendance as the result of this
     */
    public AttendanceStatus getCurrentStatus() {
        Duration timePassed = Duration.between(this.startTime, LocalDateTime.now());
        if (timePassed.toMinutes() <= SettingsManager.getInstance().getLateThresholdMinutes()) {
            return AttendanceStatus.PRESENT;
        } else {
            return AttendanceStatus.LATE;
        }
    }

    public String formatModuleSection() {
        String module = moduleSection.getModule();
        String section = moduleSection.getSection();
        return String.format("%s - %s", module, section);
    }

    public AttendanceStats attendanceStats() {
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
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
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
        int percent = (int) stats.percent();
        return String.format("%d %s", percent, "%");
    }

    private String formatDate() {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return this.startTime.format(customFormatter);
    }

    private String formatStartTime() {
        DateTimeFormatter hhmmFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return this.startTime.format(hhmmFormatter) + " - " + this.endTime.format(hhmmFormatter);
    }

    @Override
    public String[] getChipData() {
        return new String[] { this.formatModuleSection(), this.formatDate(), Integer.toString(this.getWeek()),
                this.formatStartTime(), this.formatAttendance(), this.formatRate() };
    }

    @Override
    public long[] getComparatorKeys() {
        long startDateMilliseconds = this.startTime.with(LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli();
        long startTimeSeconds = this.startTime.getHour() * 60 + this.startTime.getMinute();
        AttendanceStats stats = attendanceStats();

        int percent = 0;
        if (stats.expected() != 0) {
            percent = (int) stats.percent();
        }
        return new long[] { this.formatModuleSection().length(), startDateMilliseconds, this.getWeek(),
                startTimeSeconds, stats.present(), percent };
    }
}
