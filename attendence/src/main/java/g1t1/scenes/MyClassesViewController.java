package g1t1.scenes;

// for table 
import java.util.List;
import javafx.fxml.FXML;
import g1t1.components.table.ClassesTable;

import g1t1.models.scenes.PageController;

public class MyClassesViewController extends PageController {
    @FXML
    private ClassesTable classesHeader;

    @FXML
    private void initialize() {
        classesHeader.setClassesTable(List.of("Module", "Section", "Day", "Time", "Enrolled"));
    }
}
