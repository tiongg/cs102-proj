package g1t1.features.report;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import g1t1.features.logger.AppLogger;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;

public class XLSXReport extends ReportGenerator {

    public XLSXReport(String filepath) {
        super(filepath);
    }

    public int writeRow(Sheet sheet, int rowIndex, String[] cells, CellStyle style) {
        Row row = sheet.createRow(rowIndex);

        for (int i = 0; i < cells.length; i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellStyle(style);
            cell.setCellValue(cells[i]);
        }

        return rowIndex + 1;

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

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(getFilepath())) {
            Sheet sheet = wb.createSheet("Report");

            // Basic styles -- add in later
            CellStyle boldStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            boldStyle.setFont(bold);

            int rowIndex = 0;

            // Module Section
            String[] sectionLine = buildClassSection(session);
            rowIndex = writeRow(sheet, rowIndex, sectionLine, boldStyle);

            // Teacher
            String teacher = "Teacher: Cannot fetch teacher";
            if (report.getTeacher() != null) {
                teacher = "Teacher: " + report.getTeacher().getName();
            }
            String[] teacherRow = { teacher };
            rowIndex = writeRow(sheet, rowIndex, teacherRow, boldStyle);

            // Timestamp
            if (report.isIncludeTimeStamp()) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                rowIndex = writeRow(sheet, rowIndex, new String[] { "Report Generated on: " + now.format(fmt) }, null);
            }

            // break
            rowIndex++;

            // Header
            String[] header = buildHeader(report);
            rowIndex = writeRow(sheet, rowIndex, header, boldStyle);

            for (SessionAttendance sa : sessAttendances) {
                String[] row = buildRow(sa, report);
                rowIndex = writeRow(sheet, rowIndex, row, null);
            }

            // Auto-size columns based on header length

            wb.write(out);
            AppLogger.logf("XLSX successfully written to %s", getFilepath());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
