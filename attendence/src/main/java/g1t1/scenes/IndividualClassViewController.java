package g1t1.scenes;

import g1t1.components.table.Table;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.props.IndividualClassViewProps;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;


public class IndividualClassViewController extends PageController<IndividualClassViewProps> {
    @FXML
    private Label lblClassHeader;

    @FXML
    private Table tblSessions;

    @FXML
    public void initialize() {
        tblSessions.setTableHeaders("Class", "Date", "Time", "Attendance", "Rate");
    }

    @Override
    public void onMount() {
        ModuleSection ms = this.props.moduleSection();
        List<ClassSession> sessions = AuthenticationContext.getCurrentUser().getPastSessions().stream()
                .filter(session -> session.getModuleSection().equals(ms)).toList();
        this.tblSessions.createBody(sessions);
        this.lblClassHeader.setText(String.format("My classes - %s - %s", ms.getModule(), ms.getSection()));
    }

    @FXML
    public void classesBack() {
        Router.changePage(PageName.MyClasses);
    }

    @FXML
    public void sortAndFilter() {

    }
}
