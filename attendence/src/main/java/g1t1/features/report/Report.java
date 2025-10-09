package g1t1.features.report;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.sessions.*;
import g1t1.models.users.*;


/**
 * Generates a Report Object based on fields specified in ReportBuilder
 */
public class Report {
    private boolean includeStudentId;
    private boolean includeStatus;
    private boolean includeName;
    private boolean includeTimeStamp;
    private boolean includeConfidence;
    private boolean includeMethod;
    private boolean includeNotes;

    private Teacher currentTeacher;
    private ModuleSection moduleSection;

    public Report(ReportBuilder builder) {
        this.includeStudentId = builder.isIncludeStudentId();
        this.includeStatus = builder.isIncludeStatus();
        this.includeName = builder.isIncludeName();
        this.includeTimeStamp = builder.isIncludeTimeStamp();
        this.includeConfidence = builder.isIncludeConfidence();
        this.includeMethod = builder.isIncludeMethod();
        this.includeNotes = builder.isIncludeNotes();
        this.moduleSection = builder.getModuleSection();
        if(AuthenticationContext.getCurrentUser()!=null){
            this.currentTeacher = AuthenticationContext.getCurrentUser();
        }
    }

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

    public boolean isIncludeNotes() {
        return includeNotes;
    }

    public ModuleSection getModuleSection() {
        return moduleSection;
    }

    public Teacher getTeacher(){
        return currentTeacher;
    }

}
