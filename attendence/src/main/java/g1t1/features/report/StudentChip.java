package g1t1.features.report;

import g1t1.components.table.TableChipItem;
import g1t1.models.sessions.SessionAttendance;

import java.text.DecimalFormat;

public class StudentChip implements TableChipItem {

    private SessionAttendance attendance;

    public StudentChip(SessionAttendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("SessionAttendance cannot be null");
        }
        this.attendance = attendance;
    }

    @Override
    public String[] getChipData() {
        //table format:
        //StudentId, Name, Status, Confidence, Method
        DecimalFormat df = new DecimalFormat("#.##");
        return new String[]{
                attendance.getStudent().getId().toString(),
                attendance.getStudent().getName(),
                attendance.getStatus().toString(),
                df.format(attendance.getConfidence()),
                attendance.getMethod().toString()
        };
    }

    @Override
    public long[] getComparatorKeys() {
        return new long[]{
                0, 0, 0, (long) attendance.getConfidence(), 0
        };
    }
}