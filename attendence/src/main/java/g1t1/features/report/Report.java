package g1t1.features.report;

import g1t1.models.sessions.*;


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
    }

}
