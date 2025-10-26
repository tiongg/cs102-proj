package g1t1.features.report;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.opencsv.CSVWriter;

import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;

/**
 * Generates CSV Report which exports to specific file
 */
public class CSVReport extends ReportGenerator {
    public CSVReport(String filepath) {
        super(filepath);
    }

    @Override
    public void generate(Report report) {
        ClassSession session = report.getClassSession();
        ModuleSection section = session.getModuleSection();

        if (section == null) {
            throw new IllegalArgumentException("section cannot be null");
        }

        List<SessionAttendance> sessAttendances = session.getStudentAttendance().values().stream().toList();
        if (sessAttendances == null) {
            throw new IllegalArgumentException("SessionAttendance cannot be null");
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilepath()))) {

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
            System.out.println("File successfully written to " + getFilepath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
