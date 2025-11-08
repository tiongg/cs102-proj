package g1t1.scenes;

import g1t1.components.individualclass.StudentListItem;
import g1t1.components.table.Table;
import g1t1.components.tabs.TabSelector;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.props.IndividualClassViewProps;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.util.List;
import java.util.stream.Collectors;

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

    @FXML
    private Label lblClassHeader;

    @FXML
    private TabSelector tsSelector;

    @FXML
    private Table tblSessions;

    @FXML
    private StackPane stkFilterPane;

    @FXML
    private MenuButton mbMinAttendanceRate;

    @FXML
    private CheckComboBox<Integer> cbSelectedWeeks;

    @FXML
    private VBox vbxStudentList;

    @FXML
    private TabPane tabs;

    @FXML
    public void initialize() {
        tblSessions.setTableHeaders("Class", "Date", "Week", "Time", "Attendance", "Rate");

        for (int i = 1; i <= 13; i++) {
            cbSelectedWeeks.getItems().add(i);
        }

        cbSelectedWeeks.getCheckModel().getCheckedItems().addListener(
                (ListChangeListener<Integer>) change -> {
                    updateWeekSelectionTitle();
                }
        );

        updateWeekSelectionTitle();
        this.tsSelector.currentTabIndexProperty().subscribe((newIndex) -> {
            this.tabs.getSelectionModel().select(newIndex.intValue());
        });
    }

    @Override
    public void onMount() {
        ModuleSection ms = this.props.moduleSection();
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions()
                .stream()
                .filter(session -> session.getModuleSection().equals(ms))
                .toList();
        this.tblSessions.setTableBody(sessions);
        this.lblClassHeader.setText(String.format("My classes - %s - %s", ms.getModule(), ms.getSection()));

        this.vbxStudentList.getChildren().clear();
        this.vbxStudentList.getChildren().addAll(ms.getStudents().stream().map(StudentListItem::new).toList());

        resetSearchFields();
    }

    @FXML
    public void classesBack() {
        Router.changePage(PageName.MyClasses);
    }

    @FXML
    public void search() {
        stkFilterPane.visibleProperty().set(true);
    }

    @FXML
    public void filter() {
        ModuleSection ms = this.props.moduleSection();
        ObservableList<Integer> checkedWeeks = cbSelectedWeeks.getCheckModel().getCheckedItems();

        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions()
                .stream()
                .filter(session -> session.getModuleSection().equals(ms))
                .filter(session -> session.attendanceStats().percent() >= minAttendanceRate.get())
                .filter(session -> checkedWeeks.isEmpty() || checkedWeeks.contains(session.getWeek()))
                .toList();

        this.tblSessions.setTableBody(sessions);
        closeFilter();
    }

    @FXML
    public void closeFilter() {
        stkFilterPane.visibleProperty().set(false);
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

    private void updateWeekSelectionTitle() {
        ObservableList<Integer> checked = cbSelectedWeeks.getCheckModel().getCheckedItems();
        int totalCount = cbSelectedWeeks.getItems().size();

        if (checked.isEmpty()) {
            cbSelectedWeeks.setTitle("No weeks selected");
        } else if (checked.size() == totalCount) {
            cbSelectedWeeks.setTitle("All weeks");
        } else if (checked.size() <= 3) {
            // Show specific weeks if 3 or fewer selected
            String weeks = checked.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            cbSelectedWeeks.setTitle("Week " + weeks);
        } else {
            cbSelectedWeeks.setTitle(checked.size() + " weeks selected");
        }
    }

}
