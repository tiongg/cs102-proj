package g1t1.models.sessions;

import g1t1.models.BaseEntity;

import java.util.Date;

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
public class ClassSession extends BaseEntity {
    private ModuleSection moduleSection;
    private int week;
    private Date startTime;
    private SessionStatus sessionStatus;

    public ClassSession(ModuleSection moduleSection, int week) {
        this.moduleSection = moduleSection;
        this.sessionStatus = SessionStatus.Active;
        this.startTime = new Date();
        this.week = week;
    }

    public void endSession() {
        this.sessionStatus = SessionStatus.Ended;
    }

    public ModuleSection getModuleSection() {
        return this.moduleSection;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public int getWeek() {
        return this.week;
    }
}
