package g1t1.scenes;

import g1t1.components.TimePicker;
import g1t1.components.Toast;
import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Teacher;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StartSessionViewController extends PageController {
    private final int TOTAL_WEEKS = 13;
    private final IntegerProperty weekValue = new SimpleIntegerProperty(0);
    private final ObjectProperty<ModuleSection> moduleSectionValue = new SimpleObjectProperty<>();

    @FXML
    private MenuButton mbWeek;

    @FXML
    private MenuButton mbModuleSections;

    @FXML
    private DatePicker dpClassDate;

    @FXML
    private TimePicker tpStartTime;

    @FXML
    private Button btnStartSession;

    @FXML
    private Label lblRoom, lblExpectedStudents, lblTiming;

    @FXML
    public void startSession() {
        LocalDate localDate = dpClassDate.getValue();
        LocalDateTime sessionStartTime = tpStartTime.addToDate(localDate);

        // MockDb.getUserModuleSections(null).getFirst()
        AttendanceTaker.start(moduleSectionValue.get(), weekValue.get(), sessionStartTime);
        Router.changePage(PageName.DuringSession);
        Toast.show("Session started!", Toast.ToastType.SUCCESS);
    }

    @FXML
    public void initialize() {
        // Populate it with 13 weeks
        for (int i = 1; i <= TOTAL_WEEKS; i++) {
            MenuItem item = new MenuItem(String.format("Week %d", i));
            mbWeek.getItems().add(item);
            item.setStyle("-fx-pref-width: 385px");

            // Needed else index won't stick
            int week = i;
            item.setOnAction(e -> {
                mbWeek.setText(item.getText());
                weekValue.set(week);
            });
        }

        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            setModuleSectionSelections();
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            setModuleSectionSelections();
        });

        BooleanBinding shouldDisable = moduleSectionValue.isNull().or(weekValue.lessThanOrEqualTo(0))
                .or(dpClassDate.valueProperty().isNull());
        btnStartSession.disableProperty().bind(shouldDisable);

        lblRoom.textProperty().bind(moduleSectionValue.map(ms -> String.format("Room: %s", ms.getRoom())));
        lblExpectedStudents.textProperty().bind(moduleSectionValue.map(ms -> String.format("Expecting: %s students", ms.getStudents().size())));
        lblTiming.textProperty().bind(moduleSectionValue.map(ms -> String.format("From: %s - %s", ms.getStartTime(), ms.getEndTime())));
    }

    @Override
    public void onMount() {
        LocalDateTime dt = LocalDateTime.now();
        tpStartTime.getHourProperty().set(dt.getHour());
        tpStartTime.getMinuteProperty().set(dt.getMinute());
        dpClassDate.setValue(LocalDate.now());
    }

    private void setModuleSectionSelections() {
        Teacher user = AuthenticationContext.getCurrentUser();
        mbModuleSections.getItems().clear();
        for (ModuleSection section : user.getModuleSections()) {
            MenuItem item = new MenuItem(String.format("%s - %s", section.getModule(), section.getSection()));
            mbModuleSections.getItems().add(item);
            item.setStyle("-fx-pref-width: 385px");

            item.setOnAction(ae -> {
                mbModuleSections.setText(item.getText());
                moduleSectionValue.set(section);
            });
        }
    }
}
