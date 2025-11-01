package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.report.*;
import g1t1.models.ids.StudentID;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.SessionAttendance;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReportsViewController extends PageController {
    private final ObjectProperty<ClassSession> selectedSession = new SimpleObjectProperty<>(null);
    private final DirectoryChooser chooser = new DirectoryChooser();
    private File exportDir = null;

    @FXML
    private Table reportsTable;

    @FXML
    private Label pastReports;

    @FXML
    private Label allPastReports;

    @FXML
    private Button reportsBackBtn;

    @FXML
    private Label exportText;

    @FXML
    private MenuButton formatBtn;

    @FXML
    private Label includeText;
    @FXML
    private CheckBox cbTimestamp, cbConfidence, cbMethod;

    @FXML
    private Button exportBtn;

    @FXML
    private HBox footerBar;

    @FXML
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            switchToAllReportsView();
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            switchToAllReportsView();
        });

        chooser.setTitle("Choose a folder");
        File home = new File(System.getProperty("user.home"));
        if (home.exists()) {
            chooser.setInitialDirectory(home);
        }
    }

    @Override
    public void onMount() {
        List<ClassSession> pastSessions = AuthenticationContext.getCurrentUser().getPastSessions();
        if (pastSessions == null) {
            return;
        }
    }

    private void switchToAllReportsView() {
        selectedSession.set(null);
        pastReports.setText("Past Reports");
        allPastReports.setText("All Past Reports");
        reportsBackBtn.setVisible(false);
        exportText.setText(null);
        formatBtn.setVisible(false);
        includeText.setText(null);
        cbTimestamp.setVisible(false);
        cbConfidence.setVisible(false);
        cbMethod.setVisible(false);
        exportBtn.setVisible(false);
        footerBar.setVisible(false);
        reportsTable.setTableHeaders("Class", "Date", "Time", "Attendance", "Rate");
        reportsTable.createBody(AuthenticationContext.getCurrentUser().getPastSessions());

        reportsTable.setOnChipClick(item -> {
            if (item instanceof ClassSession cs) {
                showIndivReport(cs);
            }
        });
    }

    private void showIndivReport(ClassSession cs) {
        selectedSession.set(cs);
        exportText.setText("Export as");
        formatBtn.setVisible(true);
        includeText.setText("Include:");
        cbTimestamp.setVisible(true);
        cbConfidence.setVisible(true);
        cbMethod.setVisible(true);
        exportBtn.setVisible(true);
        footerBar.setVisible(true);
        ReportBuilder builder = new ReportBuilder().includeReport(cs).withTimeStamp().withConfidence().withMethod();
        Report report = new Report(builder);
        pastReports.setText("Past Report");
        LocalDateTime currDateTime = LocalDateTime.now();
        DateTimeFormatter formattedDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = currDateTime.format(formattedDateObj);
        allPastReports.setText("Module Session: " + cs.getModuleSection().getModule() + " - "
                + cs.getModuleSection().getSection() + "\n" + "Teacher: "
                + AuthenticationContext.getCurrentUser().getName() + "\n" + "Timestamp: " + formattedDate);
        reportsBackBtn.setVisible(true);
        reportsTable.setTableHeaders("StuID", "StuName", "Status", "Confidence", "Method");
        List<StudentChip> tableData = new ArrayList<>();
        Map<StudentID, SessionAttendance> attendance = cs.getStudentAttendance();
        Collection<SessionAttendance> allAttendances = attendance.values();
        for(SessionAttendance indivAttendance: allAttendances){
            tableData.add(new StudentChip(indivAttendance));
        }
        reportsTable.createBody(tableData);
    }

    @FXML
    private void reportsBack() {
        switchToAllReportsView();
    }

    @FXML
    private void choosePdf() {
        formatBtn.setText("PDF");
    }

    @FXML
    private void chooseCsv() {
        formatBtn.setText("CSV");
    }

    @FXML
    private void chooseXlsx() {
        formatBtn.setText("XLSX");
    }

    @FXML
    private void export() {
        if (exportDir == null) {
            Toast.show("Choose an export folder.", Toast.ToastType.WARNING);
            return;
        }

        ClassSession cs = selectedSession.get();
        String format = formatBtn.getText(); // "PDF" | "CSV" | "XLSX"
        boolean includeTimestamp = cbTimestamp.isSelected();
        boolean includeConfidence = cbConfidence.isSelected();
        boolean includeMethod = cbMethod.isSelected();

        ReportBuilder builder = new ReportBuilder().includeReport(cs);
        if (includeTimestamp) {
            builder.withTimeStamp();
        }
        if (includeConfidence) {
            builder.withConfidence();
        }
        if (includeMethod) {
            builder.withMethod();
        }
        Report report = new Report(builder);

        String mod = cs.getModuleSection().getModule().replaceAll("\\s+", "");
        String sec = cs.getModuleSection().getSection().replaceAll("\\s+", "");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String base = mod + "-" + sec + "_" + ts;

        File target = null;
        if ("PDF".equals(format)) {
            target = new File(exportDir, base + ".pdf");
            PDFReport pdfreport = new PDFReport(target.getAbsolutePath());
            pdfreport.generate(report);
        }
        if ("CSV".equals(format)) {
            target = new File(exportDir, base + ".csv");
            CSVReport csvreport = new CSVReport(target.getAbsolutePath());
            csvreport.generate(report);
        }
        if ("XLSX".equals(format)) {
            target = new File(exportDir, base + ".xlsx");
            XLSXReport xlsxreport = new XLSXReport(target.getAbsolutePath());
            xlsxreport.generate(report);
        }
        Toast.show("Exported to " + target.getAbsolutePath(), Toast.ToastType.SUCCESS);
    }

    @FXML
    private Label statusLabel;

    @FXML
    private void onChoose() {
        Window owner = statusLabel.getScene().getWindow();
        File dir = chooser.showDialog(owner);
        if (dir != null) {
            exportDir = dir;
            statusLabel.setText(dir.getAbsolutePath() + " selected!");
            chooser.setInitialDirectory(dir);
        }

    }
}
