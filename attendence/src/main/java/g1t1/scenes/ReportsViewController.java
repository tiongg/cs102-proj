package g1t1.scenes;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckComboBox;

import g1t1.components.Toast;
import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.report.CSVReport;
import g1t1.features.report.PDFReport;
import g1t1.features.report.Report;
import g1t1.features.report.ReportBuilder;
import g1t1.features.report.StudentChip;
import g1t1.features.report.XLSXReport;
import g1t1.features.report.summaries.AttendancePiechart;
import g1t1.models.ids.StudentID;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.SessionAttendance;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

record ClassOption(String userClass, String label) {
}

record AttendanceRateOption(double minRate, String label) {
};

public class ReportsViewController extends PageController {
    private static final AttendanceRateOption[] ATTENDANCE_RATE_OPTIONS = new AttendanceRateOption[] {
            new AttendanceRateOption(0, "All"), new AttendanceRateOption(50, ">50%"),
            new AttendanceRateOption(75, ">75%"), new AttendanceRateOption(100, "Full attendance"), };

    private List<ClassOption> classOptions = new ArrayList<>();

    private final ObjectProperty<ClassOption> userClass = new SimpleObjectProperty<>(null);
    private final DoubleProperty minAttendanceRate = new SimpleDoubleProperty(0);
    private final ObjectProperty<ClassSession> selectedSession = new SimpleObjectProperty<>(null);
    private final DirectoryChooser chooser = new DirectoryChooser();
    private File exportDir = null;

    @FXML
    private StackPane attendanceChartHolder;

    @FXML
    private Table reportsTable;

    @FXML
    private Label pastReports;

    @FXML
    private Label allPastReports;

    @FXML
    private Button reportsBackBtn;

    @FXML
    private HBox filter;

    @FXML
    private StackPane stkFilterPane;

    @FXML
    private MenuButton mbUserClass;

    @FXML
    private MenuButton mbMinAttendanceRate;

    @FXML
    private CheckComboBox<Integer> cbSelectedWeeks;

    @FXML
    private MenuButton formatBtn;

    @FXML
    private CheckBox cbTimestamp, cbConfidence, cbMethod;

    @FXML
    private Button exportBtn;

    @FXML
    private HBox footerBar;

    @FXML
    private VBox vbExportOptions;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            switchToAllReportsView();
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            switchToAllReportsView();
        });

        attendanceChartHolder.visibleProperty().bind(Bindings.isNotEmpty(attendanceChartHolder.getChildren()));
        attendanceChartHolder.managedProperty().bind(attendanceChartHolder.visibleProperty());
        chooser.setTitle("Choose a folder");
        File home = new File(System.getProperty("user.home"));
        if (home.exists()) {
            chooser.setInitialDirectory(home);
        }

        if (cbSelectedWeeks.getItems().isEmpty()) { // ← guard
            for (int i = 1; i <= 13; i++)
                cbSelectedWeeks.getItems().add(i);
        }

        cbSelectedWeeks.getCheckModel().getCheckedItems()
                .addListener((ListChangeListener<Integer>) change -> updateWeekSelectionTitle());
        updateWeekSelectionTitle();
    }

    @Override
    public void onMount() {
        List<ClassSession> pastSessions = AuthenticationContext.getCurrentUser().getPastSessions();
        if (pastSessions == null) {
            return;
        }

        classOptions.clear();
        classOptions.add(new ClassOption(null, "All Classes"));

        for (ClassSession cs : pastSessions) {

            String ms = cs.formatModuleSection();
            boolean exists = classOptions.contains(new ClassOption(ms, ms));
            if (!exists) {
                classOptions.add(new ClassOption(ms, ms));
            }
        }

        // Build menu items for mbClass
        mbUserClass.getItems().clear();
        for (ClassOption option : classOptions) {
            MenuItem item = new MenuItem(option.label());
            item.setOnAction(e -> {
                userClass.set(option);
                mbUserClass.setText(option.label());
            });
            mbUserClass.getItems().add(item);
        }

        // Default to "All classes"
        userClass.set(classOptions.get(0));
        mbUserClass.setText(classOptions.get(0).label());

        // Clear search fields
        resetSearchFields();
    }

    private void switchToAllReportsView() {
        selectedSession.set(null);
        pastReports.setText("Past Reports");
        allPastReports.setText("All Past Reports");
        reportsBackBtn.setVisible(false);
        filter.setVisible(true);
        formatBtn.setVisible(false);
        vbExportOptions.setVisible(false);
        cbTimestamp.setVisible(false);
        cbConfidence.setVisible(false);
        cbMethod.setVisible(false);
        exportBtn.setVisible(false);
        footerBar.setVisible(false);
        attendanceChartHolder.getChildren().clear();
        reportsTable.setTableHeaders("Class", "Date", "Week", "Time", "Attendance", "Rate");
        reportsTable.setTableBody(AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .sorted(Comparator.comparing(ClassSession::getEndTime).reversed()).toList());

        reportsTable.setOnChipClick(item -> {
            if (item instanceof ClassSession cs) {
                showIndivReport(cs);
            }
        });
    }

    private void showIndivReport(ClassSession cs) {
        selectedSession.set(cs);
        filter.setVisible(false);
        formatBtn.setVisible(true);
        vbExportOptions.setVisible(true);
        cbTimestamp.setVisible(true);
        cbTimestamp.setSelected(true);
        cbConfidence.setVisible(true);
        cbConfidence.setSelected(true);
        cbMethod.setVisible(true);
        cbMethod.setSelected(true);
        exportBtn.setVisible(true);
        footerBar.setVisible(true);
        pastReports.setText("Past Report");
        LocalDateTime currDateTime = LocalDateTime.now();
        DateTimeFormatter formattedDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = currDateTime.format(formattedDateObj);
        allPastReports.setText("Module Session: " + cs.formatModuleSection() + "\n" + "Teacher: "
                + AuthenticationContext.getCurrentUser().getName() + "\n" + "Timestamp: " + formattedDate);
        reportsBackBtn.setVisible(true);
        // piechart
        Report reportForChart = new Report(new ReportBuilder().includeReport(cs));
        PieChart chart = AttendancePiechart.create(reportForChart);
        attendanceChartHolder.getChildren().setAll(chart);

        // table
        reportsTable.setTableHeaders("StuID", "StuName", "Status", "Confidence", "Method");
        List<StudentChip> tableData = new ArrayList<>();
        Map<StudentID, SessionAttendance> attendance = cs.getStudentAttendance();
        Collection<SessionAttendance> allAttendances = attendance.values();
        for (SessionAttendance indivAttendance : allAttendances) {
            tableData.add(new StudentChip(indivAttendance));
        }
        reportsTable.setTableBody(tableData);
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
    private void onChoose() {
        Window owner = statusLabel.getScene().getWindow();
        File dir = chooser.showDialog(owner);
        if (dir != null) {
            exportDir = dir;
            statusLabel.setText(dir.getAbsolutePath() + " selected!");
            chooser.setInitialDirectory(dir);
        }
    }

    @FXML
    private void search() {
        stkFilterPane.visibleProperty().set(true);
    }

    @FXML
    public void filter() {
        ObservableList<Integer> checkedWeeks = cbSelectedWeeks.getCheckModel().getCheckedItems();

        ClassOption chosen = userClass.get();
        boolean filterByClass = chosen != null && chosen.userClass() != null;
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .filter(session -> {
                    if (!filterByClass)
                        return true;
                    String ms = session.formatModuleSection();
                    return ms.equals(chosen.userClass());
                }).filter(session -> session.attendanceStats().percent() >= minAttendanceRate.get())
                .filter(session -> checkedWeeks.isEmpty() || checkedWeeks.contains(session.getWeek())).toList();

        this.reportsTable.setTableBody(sessions);
        closeFilter();

    }

    @FXML
    public void closeFilter() {
        stkFilterPane.visibleProperty().set(false);
    }

    private void resetSearchFields() {
        if (classOptions.isEmpty()) {
            mbUserClass.setText("All classes");
        } else {
            userClass.set(classOptions.get(0));
            mbUserClass.setText(classOptions.get(0).label());
        }

        mbMinAttendanceRate.setText(ATTENDANCE_RATE_OPTIONS[0].label());
        minAttendanceRate.set(ATTENDANCE_RATE_OPTIONS[0].minRate());
        mbMinAttendanceRate.getItems().clear();
        for (AttendanceRateOption option : ATTENDANCE_RATE_OPTIONS) {
            MenuItem item = new MenuItem();
            item.setText(option.label());
            item.setOnAction((e) -> {
                minAttendanceRate.set(option.minRate());
                mbMinAttendanceRate.setText(option.label());
            });
            mbMinAttendanceRate.getItems().add(item);
        }
        cbSelectedWeeks.getCheckModel().checkAll();
        updateWeekSelectionTitle();
    }

    private void updateWeekSelectionTitle() {
        ObservableList<Integer> checked = cbSelectedWeeks.getCheckModel().getCheckedItems();
        int totalCount = cbSelectedWeeks.getItems().size();

        if (checked.isEmpty()) {
            cbSelectedWeeks.setTitle("No weeks selected");
        } else if (checked.size() == totalCount) {
            cbSelectedWeeks.setTitle("All weeks");
        } else if (checked.size() <= 3) {
            // Show specific weeks if 3 or fewer selected
            String weeks = checked.stream().map(String::valueOf).collect(Collectors.joining(", "));
            cbSelectedWeeks.setTitle("Week " + weeks);
        } else {
            cbSelectedWeeks.setTitle(checked.size() + " weeks selected");
        }
    }
}
