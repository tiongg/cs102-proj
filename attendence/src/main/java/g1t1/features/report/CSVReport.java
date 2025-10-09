package g1t1.features.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.exceptions.CsvException;
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
        if (report.isIncludeTimeStamp())
            header.add("Timestamp");
        if (report.isIncludeNotes())
            header.add("Notes");

        return header.toArray(new String[0]); // 0 added as dummy
    }

    // building row
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
        if (report.isIncludeTimeStamp())
            row.add("Temporary timestamp");
        if (report.isIncludeNotes())
            row.add("Temporary note");
        return row.toArray(new String[0]);
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
            // writer.writeNext(); takes in String[]
            writer.writeNext(buildHeader(report));
            for(Student student:students){
                writer.writeNext(buildRow(student, report));
            }
            System.out.println("File successfully written to "+filepath);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    };
}
