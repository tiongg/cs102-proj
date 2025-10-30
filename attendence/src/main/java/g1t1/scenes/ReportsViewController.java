package g1t1.scenes;

import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.features.report.*;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;
import g1t1.utils.DateUtils; //idk if this is necessary
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ReportsViewController extends PageController {

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
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            switchToAllReportsView();
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            switchToAllReportsView();
        });
    }

    @Override
    public void onMount() {
        List<ClassSession> pastSessions = AuthenticationContext.getCurrentUser().getPastSessions();
        if (pastSessions == null) {
            return;
        }
    }

    private void switchToAllReportsView() {
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
        reportsTable.setTableHeaders("Class", "Date", "Time", "Attendance", "Rate");
        reportsTable.createBody(AuthenticationContext.getCurrentUser().getPastSessions());

        reportsTable.setOnChipClick(item -> {
            if (item instanceof ClassSession cs) {
                showIndivReport(cs);
            }
        });
    }

    private void showIndivReport(ClassSession cs) {
        exportText.setText("Export as");
        formatBtn.setVisible(true);
        includeText.setText("Include:");
        cbTimestamp.setVisible(true);
        cbConfidence.setVisible(true);
        cbMethod.setVisible(true);
        exportBtn.setVisible(true);
        ReportBuilder builder = new ReportBuilder().includeReport(cs).withTimeStamp().withConfidence().withMethod();
        Report report = new Report(builder);
        pastReports.setText("Past Report");
        LocalDateTime currDateTime = LocalDateTime.now();
        DateTimeFormatter formattedDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = currDateTime.format(formattedDateObj);
        allPastReports.setText(
                "Module Session: " + cs.getModuleSection().getModule() + " - " + cs.getModuleSection().getSection() + "\n" + 
                "Teacher: " + AuthenticationContext.getCurrentUser().getName() + "\n" + 
                "Timestamp: " + formattedDate
            );
            reportsBackBtn.setVisible(true);
            reportsTable.setTableHeaders("StuID", "StuName", "Status", "Confidence", "Method");
        //TO-DO: does not show data right now 
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
        String format = formatBtn.getText(); // "PDF" | "CSV" | "XLSX"
        boolean includeTimestamp  = cbTimestamp.isSelected();
        boolean includeConfidence= cbConfidence.isSelected();
        boolean includeMethod= cbMethod.isSelected();

        //TO-DO: call actual export functions
    }
}
