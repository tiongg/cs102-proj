package g1t1.features.report;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;

public abstract class ReportGenerator {
    private String filepath; 

    public ReportGenerator(String filepath) {
        this.filepath = filepath;
    }
    
    public String getFilepath() {
        return filepath;
    }
    
    public abstract void generate(Report report);

    // helper function to count attendance status 
    public Map<String, Integer> computeAttendanceCounts(List<SessionAttendance> sessAttendances) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (SessionAttendance sa : sessAttendances) {
            String key = String.valueOf(sa.getStatus());
            m.merge(key, 1, Integer::sum);
        }
        return m;
    }

    // helper functon to build modulesection
    public String[] buildClassSection(ClassSession session) {
        ModuleSection section = session.getModuleSection();
        return new String[] {
                "Module Section: " + section.getModule() + "-" + section.getSection() + "W" + session.getWeek()}; 
    }

    // helper function to build header
    public String[] buildHeader(Report report) {
        List<String> header = new ArrayList<>();
        if (report.isIncludeStudentId())
            header.add("Student ID");
        if (report.isIncludeName())
            header.add("Name");
        if (report.isIncludeStatus())
            header.add("Status");
        if (report.isIncludeConfidence())
            header.add("Confidence");
        if (report.isIncludeMethod())
            header.add("Method");

        return header.toArray(new String[0]); // 0 added as dummy
    }

    // helper function to build student rows
    public String[] buildRow(SessionAttendance sessionAttendance, Report report) {
        List<String> row = new ArrayList<>();
        if (report.isIncludeStudentId())
            row.add(sessionAttendance.getStudent().getId().toString());
        if (report.isIncludeName())
            row.add(sessionAttendance.getStudent().getName());
        if (report.isIncludeStatus())
            row.add(sessionAttendance.getStatus().toString());
        if (report.isIncludeConfidence())
            row.add(Double.toString(sessionAttendance.getConfidence()));
        if (report.isIncludeMethod())
            row.add(sessionAttendance.getMethod().toString());
        return row.toArray(new String[0]);
    }


    
}