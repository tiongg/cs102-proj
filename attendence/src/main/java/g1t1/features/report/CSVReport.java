package g1t1.features.report;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.opencsv.CSVWriter;

import g1t1.models.sessions.*;
import g1t1.models.users.*;

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
        if (report.isIncludeNotes())
            header.add("Notes");

        return header.toArray(new String[0]); // 0 added as dummy
    }

    // helper function to build student rows
    private String[] buildRow(Student student, Report report) {
        List<String> row = new ArrayList<>();
        if (report.isIncludeStudentId())
            row.add(student.getId().toString());
        if (report.isIncludeName())
            row.add(student.getName());
        if (report.isIncludeStatus())
            row.add("Temporary status");
        if (report.isIncludeConfidence())
            row.add("Temporary confidence");
        if (report.isIncludeMethod())
            row.add("Temporary method");
        if (report.isIncludeNotes())
            row.add("Temporary note");
        return row.toArray(new String[0]);
    }

    // helper functon to build modulesection
    private String[] buildModuleSection(ModuleSection section) {
        return new String[] { "Module Section: " + section.getModule() + "-" + section.getSection() };
    }

    @Override
    public void generate(Report report) {
        ModuleSection section = report.getModuleSection();
        if (section == null) {
            throw new IllegalArgumentException("section cannot be null");
        }

        List<Student> students = section.getStudents();
        if (students == null) {
            throw new IllegalArgumentException("students cannot be null");
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filepath))) {

            // writing Module Section
            writer.writeNext(buildModuleSection(section));

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
                String[] row = { "Timestamp: " + formattedDate };
                writer.writeNext(row);
            }
            // break
            writer.writeNext(new String[] {});

            // writing Header
            writer.writeNext(buildHeader(report));

            // writing Student information
            for (Student student : students) {
                writer.writeNext(buildRow(student, report));
            }
            System.out.println("File successfully written to " + filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
