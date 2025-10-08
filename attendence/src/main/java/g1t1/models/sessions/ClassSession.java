package g1t1.models.sessions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import g1t1.components.table.TableChipItem;
import g1t1.models.BaseEntity;
import g1t1.models.users.Student;

/**
 * Class session
 */
public class ClassSession extends BaseEntity implements TableChipItem {
    private ModuleSection moduleSection;
    private int week;
    private Date startTime;
    private ArrayList<Student> presentStudents = new ArrayList<>();
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week, Date startTime, SessionStatus status) {
        this.moduleSection = moduleSection;
        this.sessionStatus = status;
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

    public ModuleSection getModuleSection() {
        return this.moduleSection;
    }

    private String formatModuleSection() {
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
        return new String[] { this.formatModuleSection(), this.formatDate(), this.moduleSection.getStartTime(),
                this.formatAttendance(), this.formatRate() };
    }
}
