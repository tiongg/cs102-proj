package g1t1.scenes;

import g1t1.features.attendencetaking.AttendanceTaker;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Teacher;
import g1t1.testing.MockDb;
import g1t1.utils.events.authentication.OnLoginEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import java.util.List;

public class StartSessionViewController extends PageController {
    private final int TOTAL_WEEKS = 13;
    private final IntegerProperty weekValue = new SimpleIntegerProperty(0);
    private final SimpleObjectProperty<ModuleSection> classValue = new SimpleObjectProperty<>();

    @FXML
    private MenuButton weekMenu;

    @FXML
    private MenuButton classMenu;

    @FXML
    public void startSession() {
        AttendanceTaker.start(classValue.get());
        Router.changePage(PageName.DuringSession);
    }

    @FXML
    public void initialize() {
        // Populate it with 13 weeks
        for (int i = 1; i <= TOTAL_WEEKS; i++) {
            MenuItem item = new MenuItem(String.format("Week %d", i));
            weekMenu.getItems().add(item);
            item.setStyle("-fx-pref-width: 400px");

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
            for (ModuleSection section : sections) {
                MenuItem item = new MenuItem(String.format("%s - %s", section.getModule(), section.getSection()));
                classMenu.getItems().add(item);
                item.setStyle("-fx-pref-width: 400px");

                item.setOnAction(ae -> {
                    classMenu.setText(item.getText());
                    classValue.set(section);
                });
            }
        });
    }
}
