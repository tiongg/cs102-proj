package g1t1.components.onboard;

import g1t1.components.register.RegistrationStep;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.ids.StudentID;
import g1t1.models.interfaces.onboard.HasStudentDetails;
import g1t1.models.scenes.Router;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.users.Teacher;
import g1t1.testing.MockDb;
import g1t1.utils.BindingUtils;
import g1t1.utils.events.authentication.OnLoginEvent;
import g1t1.utils.events.routing.OnNavigateEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

import java.util.List;

public class StudentDetails extends Tab implements RegistrationStep<HasStudentDetails> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<ModuleSection> selectedModuleSection = new SimpleObjectProperty<>(null);

    @FXML
    private TextField studentIdInput;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField emailInput;
    @FXML
    private MenuButton mbModuleSelection;

    public StudentDetails() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("OnboardDetails.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Router.emitter.subscribe(OnNavigateEvent.class, (e) -> {
            reset();
        });

        validProperty.bind(
                BindingUtils.allFilled(studentIdInput, nameInput, emailInput)
                        .and(selectedModuleSection.isNotNull())
        );
    }

    @Override
    public void reset() {
        studentIdInput.textProperty().set("");
        nameInput.textProperty().set("");
        emailInput.textProperty().set("");
    }

    @FXML
    public void initialize() {
        AuthenticationContext.emitter.subscribe(OnLoginEvent.class, (e) -> {
            Teacher user = e.user();
            List<ModuleSection> sections = MockDb.getUserModuleSections(user.getID());
            mbModuleSelection.getItems().clear();
            for (ModuleSection section : sections) {
                MenuItem item = new MenuItem(String.format("%s - %s", section.getModule(), section.getSection()));
                mbModuleSelection.getItems().add(item);
                item.setStyle("-fx-pref-width: 385px");

                item.setOnAction(ae -> {
                    mbModuleSelection.setText(item.getText());
                    selectedModuleSection.set(section);
                });
            }
        });
    }

    @Override
    public BooleanProperty validProperty() {
        return validProperty;
    }

    @Override
    public void setProperty(HasStudentDetails target) {
        target.setId(new StudentID(this.studentIdInput.getText()));
        target.setName(this.nameInput.getText());
        target.setEmail(this.emailInput.getText());
        target.setModuleSection(this.selectedModuleSection.get());
    }
}
