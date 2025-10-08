package g1t1.models.sessions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import g1t1.components.table.TableChipItem;
import g1t1.models.BaseEntity;
import g1t1.models.users.Student;

enum SessionStatus {
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
public class ClassSession extends BaseEntity implements TableChipItem {
    private ModuleSection moduleSection;
    private int week;
    private Date startTime;
    private ArrayList<Student> presentStudents;
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week, Date startTime) {
        this.moduleSection = moduleSection;
        this.sessionStatus = SessionStatus.Active;
        this.startTime = startTime;
        this.week = week;
    }

    public void endSession() {
        this.sessionStatus = SessionStatus.Ended;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public int getWeek() {
        return this.week;
    }

    private String getModuleSection() {
        String module = moduleSection.getModule();
        String section = moduleSection.getSection();
        String formatString = module + " - " + section;
        return formatString;
    }

    private String formatAttendance() {
        String formatString = presentStudents.size() + " / " + this.moduleSection.getStudents().size();
        return formatString;
    }

    private String formatRate() {
        String formatString = String.format("%d%s", presentStudents.size() / this.moduleSection.getStudents().size(),
                " %");
        return formatString;
    }

    private String formatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(this.startTime);
    }

    @Override
    public String[] getChipData() {
        return new String[] { this.getModuleSection(), this.formatDate(), this.moduleSection.getTime(),
                this.formatAttendance(), this.formatRate() };
    }
}
