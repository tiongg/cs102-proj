package g1t1.features.report;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.opencsv.CSVWriter;

import g1t1.models.sessions.*;
import g1t1.models.users.*;

/**
 * Generates CSV Report which exports to specific file
 */
public class CSVReport implements ReportGenerator {
    private String filepath;

    // constructor
    /**
     * Generate CSV file with specified filepath
     * 
     * @param filepath
     */
    public CSVReport(String filepath) {
        this.filepath = filepath; // where you want the file to be exp
    }

    // helper functon to build modulesection
    private String[] buildClassSection(ClassSession session) {
        ModuleSection section = session.getModuleSection();
        return new String[] {
                "Module Section: " + section.getModule() + "-" + section.getSection() + "W" + session.getWeek()}; 
    }

    // helper function to build header
    private String[] buildHeader(Report report) {
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
    private String[] buildRow(SessionAttendance sessionAttendance, Report report) {
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

    @Override
    public void generate(Report report) {
        ClassSession session = report.getClassSession();
        ModuleSection section = session.getModuleSection();

        if (section == null) {
            throw new IllegalArgumentException("section cannot be null");
        }

        List<SessionAttendance> sessAttendances = session.getStudentAttendance();
        if (sessAttendances == null) {
            throw new IllegalArgumentException("SessionAttendance cannot be null");
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filepath))) {

            // writing Module Section
            writer.writeNext(buildClassSection(session));

            // writing Teacher
            if (report.getTeacher() != null) {
                String[] row = { "Teacher: " + report.getTeacher() };
                writer.writeNext(row);
            } else {
                writer.writeNext(new String[] { "Teacher: Cannot fetch teacher" });
            }

            // writing TimeStamp
            if (report.isIncludeTimeStamp()) {
                LocalDateTime currDateTime = LocalDateTime.now();
                DateTimeFormatter formattedDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedDate = currDateTime.format(formattedDateObj);
                String[] row = { "Report Generated on: " + formattedDate };
                writer.writeNext(row);
            }
            // break
            writer.writeNext(new String[] {});

            // writing Header
            writer.writeNext(buildHeader(report));

            // writing Student information based on SessionAttendance
            for (SessionAttendance sessionAttendance : sessAttendances) {
                writer.writeNext(buildRow(sessionAttendance, report));
            }
            System.out.println("File successfully written to " + filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
