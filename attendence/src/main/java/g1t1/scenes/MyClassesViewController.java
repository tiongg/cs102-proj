package g1t1.scenes;

// for table 

import g1t1.components.TimePicker;
import g1t1.components.Toast;
import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.testing.MockDb;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.List;

public class MyClassesViewController extends PageController {
    private final IntegerProperty newClassWeekValue = new SimpleIntegerProperty(-1);

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
    private MenuButton mbDay;
    @FXML
    private TimePicker tfStart;
    @FXML
    private TimePicker tfEnd;
    @FXML
    private TextField tfRoom;


    @FXML
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            switchToModuleView();
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            switchToModuleView();
        });

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < days.length; i++) {
            MenuItem item = new MenuItem(String.format("%s", days[i]));
            mbDay.getItems().add(item);
            item.setStyle("-fx-pref-width: 385px");
            // Needed else index won't stick
            int dayOfWeek = i;
            item.setOnAction(e -> {
                mbDay.setText(item.getText());
                newClassWeekValue.set(dayOfWeek);
            });
        }
    }

    private void showSessions(ModuleSection ms) {
        myClasses.setText("My Classes - " + ms.getModule() + " - " + ms.getSection());
        classesBackBtn.setVisible(true);
        addClassBtn.setVisible(false);
        availableClasses.setText(null);
        classesTable.setTableHeaders(List.of("Class", "Date", "Time", "Attendance", "Rate"));
        List<ClassSession> sessions = MockDb.getPastSessions(AuthenticationContext.getCurrentUser().getID()).stream()
                .filter(session -> session.getModuleSection().equals(ms)).toList();

        classesTable.createBody(sessions);
    }

    private void switchToModuleView() {
        myClasses.setText("My Classes");
        availableClasses.setText("Available Classes");
        classesBackBtn.setVisible(false);
        addClassOverlay.setVisible(false);
        addClassBtn.setVisible(true);
        classesTable.setTableHeaders(List.of("Module", "Section", "Day", "Time", "Enrolled"));
        classesTable.createBody(AuthenticationContext.getCurrentUser().getModuleSections());

        classesTable.setOnChipClick(item -> {
            if (item instanceof ModuleSection ms) {
                showSessions(ms);
            }
        });
    }

    @FXML
    private void classesBack() {
        switchToModuleView();
    }

    @FXML
    private void openAddClass() {
        tfModule.clear();
        tfSection.clear();
        tfTerm.setText(ModuleSection.getCurrentAYTerm());
        tfTerm.setEditable(false);
        tfStart.resetTime();
        tfEnd.resetTime();
        newClassWeekValue.set(-1);
        mbDay.setText("Select a day...");
        tfRoom.clear();
        addClassOverlay.setVisible(true);
    }

    @FXML
    private void closeAddClass() {
        addClassOverlay.setVisible(false);
    }

    @FXML
    private void createClass() {
        String module = tfModule.getText();
        String section = tfSection.getText();
        String term = tfTerm.getText();
        int dayOfWeek = newClassWeekValue.intValue();
        String start = tfStart.getFormattedTime();
        String end = tfEnd.getFormattedTime();
        String room = tfRoom.getText();

        if (module.isBlank() || section.isBlank() || dayOfWeek == -1) {
            Toast.show("Please fill in all the fields", Toast.ToastType.ERROR);
            return;
        }


        ModuleSection newModule = new ModuleSection(module, section, term, room, dayOfWeek, start, end);

//        System.out.println(module);
//        System.out.println(section);
//        System.out.println(term);
//        System.out.println(dayOfWeek);
//        System.out.println(start);
//        System.out.println(end);
//        System.out.println(room);

        AuthenticationContext.getCurrentUser().getModuleSections().add(newModule);
        AuthenticationContext.triggerUserUpdate();

        Toast.show("Class Created!", Toast.ToastType.SUCCESS);
    }

}
