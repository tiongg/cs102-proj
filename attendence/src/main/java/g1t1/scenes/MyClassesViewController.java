package g1t1.scenes;

import java.util.ArrayList;
// for table 
import java.util.List;
import javafx.fxml.FXML;
import g1t1.components.table.Table;
import g1t1.components.table.TableChipItem;
import g1t1.models.scenes.PageController;
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
    }
}
