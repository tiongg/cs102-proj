package g1t1.scenes;

import java.util.ArrayList;
// for table 
import java.util.List;
import javafx.fxml.FXML;
import g1t1.components.table.Table;
import g1t1.components.table.TableChipItem;
import g1t1.models.scenes.PageController;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;

public class MyClassesViewController extends PageController {
    @FXML
    private Table classesTable;

    @FXML
    private void initialize() {
        classesTable.setTable(List.of("Module", "Section", "Day", "Time", "Enrolled"));

        List<ModuleSection> classes = new ArrayList<>();
        classes.add(new ModuleSection("CS102", "G1", 3, "08:00 - 11:30"));
        classes.add(new ModuleSection("CS102", "G2", 3, "15:30 - 19:45"));

        classesTable.createBody(classes);

        classesTable.setOnChipClick(item -> {
            if (item instanceof ModuleSection ms) {
                showSessions(ms);
            }
        });
    }

    private void showSessions(ModuleSection ms) {
        classesTable.setTable(List.of("Class", "Date", "Time", "Attendance", "Rate"));

        List<ClassSession> sessions = new ArrayList<>();
        sessions.add(new ClassSession(ms, "2025-09-01", "08:00 - 09:30"));
        sessions.add(new ClassSession(ms, "2025-09-08", "08:00 - 09:30"));

        classesTable.createBody(sessions);
    }
}
