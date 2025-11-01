package g1t1.scenes;

import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.props.IndividualClassViewProps;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.CheckComboBox;

import java.util.List;

record AttendanceRateOption(double minRate, String label) {
};

public class IndividualClassViewController extends PageController<IndividualClassViewProps> {
    private static final AttendanceRateOption[] ATTENDANCE_RATE_OPTIONS = new AttendanceRateOption[]{
            new AttendanceRateOption(0, "All"),
            new AttendanceRateOption(0.5, ">50%"),
            new AttendanceRateOption(0.75, ">75%"),
            new AttendanceRateOption(1, "Full attendance"),
    };

    private final DoubleProperty minAttendanceRate = new SimpleDoubleProperty(0);
    private final ObservableList<Integer> selectedWeeks = FXCollections.observableArrayList();

    @FXML
    private Label lblClassHeader;

    @FXML
    private Table tblSessions;

    @FXML
    private StackPane stkSearchPane;

    @FXML
    private MenuButton mbMinAttendanceRate;

    @FXML
    private CheckComboBox<Integer> cbSelectedWeeks;

    @FXML
    public void initialize() {
        tblSessions.setTableHeaders("Class", "Date", "Week", "Time", "Attendance", "Rate");

        for (int i = 1; i <= 13; i++) {
            cbSelectedWeeks.getItems().add(i);
        }

        cbSelectedWeeks.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener<Integer>) change -> {
            selectedWeeks.setAll(cbSelectedWeeks.getCheckModel().getCheckedItems());
        });
    }

    @Override
    public void onMount() {
        ModuleSection ms = this.props.moduleSection();
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .filter(session -> session.getModuleSection().equals(ms)).toList();
        this.tblSessions.setTableBody(sessions);
        this.lblClassHeader.setText(String.format("My classes - %s - %s", ms.getModule(), ms.getSection()));

        resetSearchFields();
    }

    @FXML
    public void classesBack() {
        Router.changePage(PageName.MyClasses);
    }

    @FXML
    public void search() {
        stkSearchPane.visibleProperty().set(true);
    }

    @FXML
    public void filter() {
        ModuleSection ms = this.props.moduleSection();
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions()
                .stream()
                .filter(session -> session.getModuleSection().equals(ms))
                .filter(session -> session.attendanceStats().percent() >= minAttendanceRate.get())
                .toList();

        this.tblSessions.setTableBody(sessions);
    }

    private void resetSearchFields() {
        mbMinAttendanceRate.setText(ATTENDANCE_RATE_OPTIONS[0].label());
        minAttendanceRate.set(ATTENDANCE_RATE_OPTIONS[0].minRate());
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

    }
}
