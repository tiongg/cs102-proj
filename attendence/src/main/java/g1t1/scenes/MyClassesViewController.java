package g1t1.scenes;

// for table 
import java.util.List;

import g1t1.components.TimePicker;
import g1t1.components.Toast;
import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.testing.MockDb;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class MyClassesViewController extends PageController {

    @FXML
    private Table classesTable;

    @FXML
    private Label myClasses;

    @FXML
    private Label availableClasses;

    @FXML
    private Button classesBackBtn;

    @FXML
    private Button addClassBtn;

    @FXML
    private StackPane addClassOverlay;
    @FXML
    private TextField tfModule;
    @FXML
    private TextField tfSection;
    @FXML
    private TextField tfTerm;
    @FXML
    private ChoiceBox<String> cbDay;
    @FXML
    private TimePicker tfStart;
    @FXML
    private TimePicker tfEnd;
    @FXML
    private TextField tfRoom;

    private List<ModuleSection> cacheModuleSections;

    @FXML
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            classesTable.setTable(List.of("Module", "Section", "Day", "Time", "Enrolled"));

            classesTable.createBody(MockDb.getUserModuleSections(e.user().getID()));
            cacheModuleSections = MockDb.getUserModuleSections(e.user().getID());

            classesTable.setOnChipClick(item -> {
                if (item instanceof ModuleSection ms) {
                    showSessions(ms);
                }
            });
        });
    }

    private void showSessions(ModuleSection ms) {

        myClasses.setText("My Classes - " + ms.getModule() + " - " + ms.getSection());
        classesBackBtn.setVisible(true);
        addClassBtn.setVisible(false);
        availableClasses.setText(null);
        classesTable.setTable(List.of("Class", "Date", "Time", "Attendance", "Rate"));
        List<ClassSession> sessions = MockDb.getPastSessions(AuthenticationContext.getCurrentUser().getID()).stream()
                .filter(session -> session.getModuleSection().equals(ms)).toList();

        classesTable.createBody(sessions);
    }

    private void moduleViews() {

        myClasses.setText("My Classes");
        availableClasses.setText("Available Classes");
        classesBackBtn.setVisible(false);
        addClassBtn.setVisible(true);
        classesTable.setTable(List.of("Module", "Section", "Day", "Time", "Enrolled"));

        classesTable.createBody(cacheModuleSections);

        classesTable.setOnChipClick(item -> {
            if (item instanceof ModuleSection ms) {
                showSessions(ms);
            }
        });

    }

    @FXML
    private void classesBack() {
        // System.out.println("going back");
        moduleViews();
    }

    @FXML
    private void openAddClass() {
        if (cbDay.getItems().isEmpty()) {
            cbDay.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        }
        tfModule.clear();
        tfSection.clear();
        tfTerm.setText(ModuleSection.getCurrentAYTerm());
        tfTerm.setEditable(false);
        tfStart.resetTime();
        tfEnd.resetTime();
        cbDay.getSelectionModel().clearSelection();
        tfRoom.clear();
        addClassOverlay.setVisible(true);
    }

    @FXML
    private void closeAddClass() {
        addClassOverlay.setVisible(false);
    }

    @FXML
    private void createClass() {
        // Format of start and end, String (HH:MM)

        String module = tfModule.getText();
        String section = tfSection.getText();
        String term = tfTerm.getText();
        String dayName = cbDay.getValue();
        String start = tfStart.getFormattedTime();
        String end = tfEnd.getFormattedTime();
        String room = tfRoom.getText();

        if (module.isBlank() || section.isBlank() || dayName == null) {

            Toast.show("Please fill in all the fields", Toast.ToastType.ERROR);
            return;
        }

        int dayNum = switch (dayName) {
        case "Monday" -> 0;
        case "Tuesday" -> 1;
        case "Wednesday" -> 2;
        case "Thursday" -> 3;
        case "Friday" -> 4;
        case "Saturday" -> 5;
        case "Sunday" -> 6;
        default -> -1;
        };

        // ModuleSection newModule = new ModuleSection(module, section, term, room,
        // dayNum, start, end);

        // tiong said to just print to terminal :)
        System.out.println(module);
        System.out.println(section);
        System.out.println(term);
        System.out.println(dayNum);
        System.out.println(start);
        System.out.println(end);
        System.out.println(room);
        // cacheModuleSections.add(newModule);

        Toast.show("Class Created!", Toast.ToastType.SUCCESS);
        moduleViews();
        addClassOverlay.setVisible(false);
    }

}
