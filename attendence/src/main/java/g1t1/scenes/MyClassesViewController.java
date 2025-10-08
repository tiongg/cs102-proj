package g1t1.scenes;

// for table 
import java.util.List;

import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.testing.MockDb;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MyClassesViewController extends PageController {
    @FXML
    private Table classesTable;

    @FXML
    private Label myClasses;

    @FXML
    private Label availableClasses;

    @FXML
    private Button classesBack;

    @FXML
    private Button addClass;

    @FXML
    private void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            classesTable.setTable(List.of("Module", "Section", "Day", "Time", "Enrolled"));

            classesTable.createBody(MockDb.getUserModuleSections(e.user().getID()));

            classesTable.setOnChipClick(item -> {
                if (item instanceof ModuleSection ms) {
                    showSessions(ms);
                }
            });
        });
    }

    private void showSessions(ModuleSection ms) {

        myClasses.setText("My Classes - " + ms.getModule() + " - " + ms.getSection());
        classesBack.setVisible(true);
        addClass.setVisible(false);
        availableClasses.setText(null);
        classesTable.setTable(List.of("Class", "Date", "Time", "Attendance", "Rate"));
        List<ClassSession> sessions = MockDb.getPastSessions(AuthenticationContext.getCurrentUser().getID()).stream()
                .filter(session -> session.getModuleSection().equals(ms)).toList();

        classesTable.createBody(sessions);
    }
}
