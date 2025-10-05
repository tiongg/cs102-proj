package g1t1.scenes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import g1t1.components.TimePicker;
import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Teacher;
import g1t1.testing.MockDb;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class StartSessionViewController extends PageController {
    private final int TOTAL_WEEKS = 13;
    private final IntegerProperty weekValue = new SimpleIntegerProperty(0);
    private final SimpleObjectProperty<ModuleSection> classValue = new SimpleObjectProperty<>();

    @FXML
    private MenuButton weekMenu;

    @FXML
    private MenuButton classMenu;

    @FXML
    private DatePicker dpClassDate;

    @FXML
    private TimePicker tpStartTime;

    @FXML
    private Button btnStartSession;

    @FXML
    public void startSession() {
        LocalDate localDate = dpClassDate.getValue();
        LocalDateTime sessionStartTime = tpStartTime.addToDate(localDate);

        // System.out.println(sessionStartTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy
        // HH:mm")));
        AttendanceTaker.start(classValue.get(), weekValue.get());
        Router.changePage(PageName.DuringSession);
    }

    @FXML
    public void initialize() {
        // Populate it with 13 weeks
        for (int i = 1; i <= TOTAL_WEEKS; i++) {
            MenuItem item = new MenuItem(String.format("Week %d", i));
            weekMenu.getItems().add(item);
            item.setStyle("-fx-pref-width: 385px");

            // Needed else index won't stick
            int week = i;
            item.setOnAction(e -> {
                weekMenu.setText(item.getText());
                weekValue.set(week);
            });
        }

        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            Teacher user = e.user();
            List<ModuleSection> sections = MockDb.getUserModuleSections(user.getID());
            classMenu.getItems().clear();
            for (ModuleSection section : sections) {
                MenuItem item = new MenuItem(String.format("%s - %s", section.getModule(), section.getSection()));
                classMenu.getItems().add(item);
                item.setStyle("-fx-pref-width: 385px");

                item.setOnAction(ae -> {
                    classMenu.setText(item.getText());
                    classValue.set(section);
                });
            }
        });

        BooleanBinding shouldDisable = classValue.isNull().or(weekValue.lessThanOrEqualTo(0));
        btnStartSession.disableProperty().bind(shouldDisable);
    }

    @Override
    public void onMount() {
        LocalDateTime dt = LocalDateTime.now();
        tpStartTime.getHourProperty().set(dt.getHour());
        tpStartTime.getMinuteProperty().set(dt.getMinute());
        dpClassDate.setValue(LocalDate.now());
    }
}
