package g1t1.features.report;

import g1t1.models.sessions.*;

/**
 * Takes in ModuleSection and acts as builder for Report
 */
public class ReportBuilder {
    private boolean includeStudentId = true;
    private boolean includeStatus = true;
    private boolean includeName = true;
    private boolean includeTimeStamp = false;
    private boolean includeConfidence = false;
    private boolean includeMethod = false;
    private ClassSession classSession;

    /*
     * Notes: ClassSession Contains: 
     * - moduleSection: ModuleSection object 
     * - week: int 
     * - startTime: Date 
     * - sessionStatus: SessionStatus object
     * 
     * SessionStatus Contains: 
     * - Active: Session is ongoing 
     * - Ended: Session has
     * ended
     * 
     * ModuleSection Contains: 
     * - module: String (eg. CS102) 
     * - section: String (eg.G2) 
     * - students: List<Student>
     * 
     * Student Contains: 
     *  - id: String (eg. "01466890")
     * 
     */

    // to add data of modulesection that you want data from
    public ReportBuilder includeReport(ClassSession classSession) {
        this.classSession = classSession;
        return this;
    }

    public ReportBuilder withTimeStamp() {
        this.includeTimeStamp = true;
        return this;
    }

    public ReportBuilder withConfidence() {
        this.includeConfidence = true;
        return this;
    }

    public ReportBuilder withMethod() {
        this.includeMethod = true;
        return this;
    }

    // getters
    public boolean isIncludeStudentId() {
        return includeStudentId;
    }

    public boolean isIncludeStatus() {
        return includeStatus;
    }

    public boolean isIncludeName() {
        return includeName;
    }

    public boolean isIncludeTimeStamp() {
        return includeTimeStamp;
    }

    public boolean isIncludeConfidence() {
        return includeConfidence;
    }

    public boolean isIncludeMethod() {
        return includeMethod;
    }

    public ClassSession getClassSession() {
        return classSession;
    }

}
