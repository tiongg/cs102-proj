package g1t1.scenes;

import g1t1.models.scenes.PageController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.opencv.core.Core;

public class DebugViewController extends PageController {
    @FXML
    Label lbl_javaVersion;
    @FXML
    Label lbl_opencvVersion;
    @FXML
    Label lbl_javafxVersion;
    
    @FXML
    public void initialize() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        String opencvVersion = Core.VERSION;

        lbl_javaVersion.setText("Java version: " + javaVersion);
        lbl_opencvVersion.setText("Opencv version: " + opencvVersion);
        lbl_javafxVersion.setText("JavaFX version: " + javafxVersion);
    }
}
