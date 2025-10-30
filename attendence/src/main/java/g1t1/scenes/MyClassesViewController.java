package g1t1.scenes;

import g1t1.components.TimePicker;
import g1t1.components.Toast;
import g1t1.components.table.Table;
import g1t1.db.DSLInstance;
import g1t1.db.module_sections.ModuleSectionRepository;
import g1t1.db.module_sections.ModuleSectionRepositoryJooq;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.scenes.PageController;
import g1t1.models.scenes.PageName;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.props.IndividualClassViewProps;
import g1t1.utils.DateUtils;
import g1t1.utils.events.authentication.OnUserUpdateEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.jooq.exception.DataAccessException;

import java.sql.SQLException;
import java.util.List;

public class MyClassesViewController extends PageController {
    private final IntegerProperty newClassWeekValue = new SimpleIntegerProperty(-1);

    @FXML
    private Table classesTable;
    @FXML
    private StackPane stkEmptyPrompt;

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
        classesTable.setTableHeaders("Module", "Section", "Day", "Time", "Enrolled");
        classesTable.setOnChipClick(item -> {
            if (item instanceof ModuleSection ms) {
                showSessions(ms);
            }
        });
        AuthenticationContext.emitter.subscribe(OnUserUpdateEvent.class, (e) -> {
            updateModuleSections();
        });

        for (int i = 0; i < DateUtils.daysOfWeek.length; i++) {
            MenuItem item = new MenuItem(String.format("%s", DateUtils.daysOfWeek[i]));
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
        Router.changePage(PageName.IndividualClass, new IndividualClassViewProps(ms));
    }

    private void updateModuleSections() {
        addClassOverlay.setVisible(false);
        List<ModuleSection> moduleSections = AuthenticationContext.getCurrentUser().getModuleSections();
        stkEmptyPrompt.visibleProperty().set(moduleSections.isEmpty());
        classesTable.setTableBody(moduleSections);
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

        try (DSLInstance dslInstance = new DSLInstance()) {
            ModuleSectionRepository moduleSectionRepository = new ModuleSectionRepositoryJooq(dslInstance.dsl);
            String moduleSectionId = moduleSectionRepository.create(
                    module,
                    section,
                    term,
                    dayOfWeek,
                    start, end,
                    room,
                    AuthenticationContext.getCurrentUser().getID().toString()
            );
            ModuleSection newModule = new ModuleSection(moduleSectionId, module, section, term, room, dayOfWeek, start, end);

            AuthenticationContext.getCurrentUser().getModuleSections().add(newModule);
            AuthenticationContext.triggerUserUpdate();

            Toast.show("Class Created!", Toast.ToastType.SUCCESS);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
    }

}
