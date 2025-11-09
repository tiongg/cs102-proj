package g1t1.scenes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.CheckComboBox;

import g1t1.components.individualclass.StudentListItem;
import g1t1.components.table.Table;
import g1t1.components.tabs.TabSelector;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Student;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

record AttendanceRateOption(double minRate, String label) {
};

public class IndividualClassViewController extends PageController<IndividualClassViewProps> {
    private static final AttendanceRateOption[] ATTENDANCE_RATE_OPTIONS = new AttendanceRateOption[] {
            new AttendanceRateOption(0, "All"), new AttendanceRateOption(50, ">50%"),
            new AttendanceRateOption(75, ">75%"), new AttendanceRateOption(1, "Full attendance"), };

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
    private TextField tfStudentSearch;

    private List<Student> originalStudentList;
    private ModuleSection currentModuleSection;

    @FXML
    public void initialize() {
        tblSessions.setTableHeaders("Class", "Date", "Week", "Time", "Attendance", "Rate");

        for (int i = 1; i <= 13; i++) {
            cbSelectedWeeks.getItems().add(i);
        }

        cbSelectedWeeks.getCheckModel().getCheckedItems().addListener((ListChangeListener<Integer>) change -> {
            updateWeekSelectionTitle();
        });

        // Add listener for student search
        tfStudentSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filterStudents(newVal);
        });

        // Show/hide search field based on selected tab
        this.tsSelector.currentTabIndexProperty().subscribe((newIndex) -> {
            int tabIndex = newIndex.intValue();
            this.tabs.getSelectionModel().select(tabIndex);

            // Show search field only on student list tab (index 1)
            tfStudentSearch.setVisible(tabIndex == 1);
            tfStudentSearch.setManaged(tabIndex == 1);
        });

        updateWeekSelectionTitle();
    }

    @Override
    public void onMount() {
        ModuleSection ms = this.props.moduleSection();
        this.currentModuleSection = ms;
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .filter(session -> session.getModuleSection().equals(ms))
                .sorted(Comparator.comparing(ClassSession::getEndTime).reversed()).toList();
        this.tblSessions.setTableBody(sessions);
        this.lblClassHeader.setText(String.format("My classes - %s - %s", ms.getModule(), ms.getSection()));

        // Store original student list for search/filtering
        this.originalStudentList = ms.getStudents();
        this.vbxStudentList.getChildren().clear();
        this.vbxStudentList.getChildren().addAll(this.originalStudentList.stream().map(StudentListItem::new).toList());

        // Clear search field when switching classes
        tfStudentSearch.clear();
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

        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .filter(session -> session.getModuleSection().equals(ms))
                .filter(session -> session.attendanceStats().percent() >= minAttendanceRate.get())
                .filter(session -> checkedWeeks.isEmpty() || checkedWeeks.contains(session.getWeek())).toList();

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
            String weeks = checked.stream().map(String::valueOf).collect(Collectors.joining(", "));
            cbSelectedWeeks.setTitle("Week " + weeks);
        } else {
            cbSelectedWeeks.setTitle(checked.size() + " weeks selected");
        }
    }

    /**
     * Filters the student list based on the search query. Searches by student name,
     * ID, or email (case-insensitive).
     *
     * @param query the search query string
     */
    private void filterStudents(String query) {
        if (originalStudentList == null) {
            return;
        }

        vbxStudentList.getChildren().clear();

        if (query == null || query.isBlank()) {
            // Show all students
            vbxStudentList.getChildren().addAll(originalStudentList.stream().map(StudentListItem::new).toList());
        } else {
            // Filter students by name, ID, or email
            String lowerQuery = query.toLowerCase().trim();
            List<StudentListItem> filteredStudents = originalStudentList.stream()
                    .filter(student -> student.getName().toLowerCase().contains(lowerQuery)
                            || student.getId().toString().contains(lowerQuery)
                            || student.getEmail().toLowerCase().contains(lowerQuery))
                    .map(StudentListItem::new).toList();

            vbxStudentList.getChildren().addAll(filteredStudents);

            // Show empty state if no results
            if (filteredStudents.isEmpty()) {
                Label noResultsLabel = new Label("No students found matching \"" + query + "\"");
                noResultsLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 14;");
                vbxStudentList.getChildren().add(noResultsLabel);
            }
        }
    }

}
