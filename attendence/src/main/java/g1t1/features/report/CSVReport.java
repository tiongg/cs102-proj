package g1t1.features.report;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVWriter;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.features.logger.AppLogger;
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

        // attendance summary
        Map<String, Integer> counts = computeAttendanceCounts(sessAttendances);
        int total = sessAttendances.size();
        
        int attended = 0;

        for (String key : counts.keySet()) {
            String status = key;
            int count = counts.get(key);

            if (status.equals("PRESENT") || status.equals("LATE")) {
                attended += count;
            }
        }

        double overallPercentage = 0.0;
        if (total != 0) {
            overallPercentage = attended * 100 / total;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(getFilepath()))) {

            // writing Module Section
            writer.writeNext(buildClassSection(session));

            // writing Teacher
            if (report.getTeacher() != null) {
                String[] row = { "Teacher: " + report.getTeacher().getName() };
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
        
            //summary
            writer.writeNext(new String[] { });
            writer.writeNext(new String[] { "Attendance Summary" });

            // per-status table
            writer.writeNext(new String[] { "Status", "Count", "Percent" });
            for (String key : counts.keySet()) {
                int c = counts.getOrDefault(key, 0);
                double percentage = 0.0;
                if (total != 0) {
                    percentage = (c * 100) / total;
                }
                writer.writeNext(new String[] {
                        key.toString(),
                        String.valueOf(c),
                        String.format("%.1f%%", percentage)
                });
            }

            // overall attendance rate counts present and late ppl
            writer.writeNext(new String[] {});
            String ratioText = "=\"" + attended + " / " + total + "\"";
            writer.writeNext(new String[] {"Overall attendance rate", ratioText, String.format("%.1f%%", overallPercentage)});

            // break
            writer.writeNext(new String[] { });

            // writing Header
            writer.writeNext(buildHeader(report));

            // writing Student information based on SessionAttendance
            for (SessionAttendance sessionAttendance : sessAttendances) {
                writer.writeNext(buildRow(sessionAttendance, report));
            }
            AppLogger.logf("CSV successfully written to %s", getFilepath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
