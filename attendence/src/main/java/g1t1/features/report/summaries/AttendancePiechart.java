package g1t1.features.report.summaries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import g1t1.db.attendance.AttendanceStatus;
import g1t1.features.report.Report;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.SessionAttendance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

/**
 * Create Piechart based on attendance
 * 
 * @param report
 */
public class AttendancePiechart {
    public AttendancePiechart() {
    }

    public static PieChart create(Report report) {
        ClassSession session = report.getClassSession();
        if (session == null) {
            PieChart emptychart = new PieChart(FXCollections.observableArrayList());
            emptychart.setTitle("No session data");
            return emptychart;
        }
        List<SessionAttendance> studentAttendance = session.getStudentAttendance();

        // stores the values for each attendancestatus
        Map<AttendanceStatus, Integer> attendenceCounts = new HashMap<>();
        for (SessionAttendance attendance : studentAttendance) {
            AttendanceStatus status = attendance.getStatus();
            attendenceCounts.merge(status, 1, Integer::sum); // increments status by 1
        }

        // configuring piechart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (AttendanceStatus status : attendenceCounts.keySet()) {
            int count = attendenceCounts.get(status);

            if (count > 0) {
                String pieslicename = status.toString();
                pieChartData.add(new PieChart.Data(pieslicename, count));
            }
        }

        final PieChart piechart = new PieChart(pieChartData);
        piechart.setTitle("Session Attendance Breakdown");
        piechart.setLabelsVisible(true);

        return piechart;
    }
}
