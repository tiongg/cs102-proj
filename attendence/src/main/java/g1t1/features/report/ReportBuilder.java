package g1t1.features.report;
import g1t1.models.sessions.*;

public class ReportBuilder {
    private boolean includeStudentId = true;
    private boolean includeStatus = true;
    private boolean includeName = true;
    private boolean includeTimeStamp = false;
    private boolean includeConfidence = false;
    private boolean includeMethod = false;
    private boolean includeNotes = false;
    private ModuleSection moduleSection;

    /*
        Notes:
        ClassSession
        Contains:
        - moduleSection: ModuleSection object
        - week: int
        - startTime: Date
        - sessionStatus: SessionStatus object

        SessionStatus
        Contains:
        - Active: Session is ongoing
        - Ended: Session has ended

        ModuleSection
        Contains:
        - module: String (eg. CS102)
        - section: String (eg. G2)
        - students: List<Student>

        Student 
        Contains:
        - id: String (eg. "01466890")


     */

    // to add data of modulesection that you want data from
    public ReportBuilder includeReport(ModuleSection moduleSection){
        this.moduleSection = moduleSection;
        return this;
    }

    public ReportBuilder withTimeStamp(){
        this.includeTimeStamp = true;
        return this;
    }
    public ReportBuilder withConfidence(){
        this.includeConfidence = true;
        return this;
    }
    public ReportBuilder withMethod(){
        this.includeMethod = true;
        return this;
    }
    public ReportBuilder withNotes(){
        this.includeNotes = true;
        return this;
    }

    //getters
    public boolean isIncludeStudentId() { return includeStudentId; }
    public boolean isIncludeStatus() { return includeStatus; }
    public boolean isIncludeName() { return includeName; }
    public boolean isIncludeTimeStamp() { return includeTimeStamp; }
    public boolean isIncludeConfidence() { return includeConfidence; }
    public boolean isIncludeMethod() { return includeMethod; }
    public boolean isIncludeNotes() { return includeNotes; }
    public ModuleSection getModuleSection() { return moduleSection; }

}
