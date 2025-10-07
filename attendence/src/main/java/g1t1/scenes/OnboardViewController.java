package g1t1.scenes;

import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.db.DSLInstance;
import g1t1.db.student_face_images.StudentFaceImageRepository;
import g1t1.db.student_face_images.StudentFaceImageRepositoryJooq;
import g1t1.db.students.StudentRepository;
import g1t1.db.students.StudentRepositoryJooq;
import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.PageController;
import g1t1.models.users.RegisterStudent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import org.jooq.exception.DataAccessException;

import java.sql.SQLException;

public class OnboardViewController extends PageController {
    private final BooleanProperty canNext = new SimpleBooleanProperty(false);

    private RegisterStudent registerStudent;

    @FXML
    private StepperControl stepper;
    @FXML
    private TabPane tabs;
    @FXML
    private Label backText;
    @FXML
    private Label nextText;
    @FXML
    private HBox backButton;
    @FXML
    private HBox nextButton;

    @FXML
    public void initialize() {
        stepper.currentIndexProperty().addListener((obv, oldIndex, newIndex) -> {
            if (stepper.isLast()) {
                nextText.setText("Done!");
            } else {
                nextText.setText("Next");
            }

            backText.setText("Back");

            getCurrentStep().setProperty(this.registerStudent);
            getCurrentStep().onUnmount();
            tabs.getSelectionModel().select(newIndex.intValue());
            getCurrentStep().onMount(this.registerStudent);

            updateButtonListeners();
        });

        updateButtonListeners();
        canNext.addListener((obs, oldValue, nextAllowed) -> {
            if (nextAllowed) {
                nextButton.getStyleClass().remove("disabled");
            } else {
                nextButton.getStyleClass().add("disabled");
            }
        });
        stepper.currentIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                backButton.getStyleClass().add("disabled");
            } else {
                backButton.getStyleClass().remove("disabled");
            }
        });
    }

    @Override
    public void onMount() {
        reset();
    }

    private void reset() {
        stepper.setCurrentIndex(0);
        this.registerStudent = new RegisterStudent();
        for (Tab tab : this.tabs.getTabs()) {
            RegistrationStep<HasProperty> step = (RegistrationStep<HasProperty>) tab;
            step.reset();
        }
    }

    private void updateButtonListeners() {
        canNext.unbind();
        RegistrationStep<HasProperty> step = getCurrentStep();
        canNext.bind(step.validProperty());
    }

    public void next() {
        if (!canNext.get()) {
            return;
        }

        if (!stepper.isLast()) {
            stepper.next();
        } else {
            boolean success = saveToDb();
            if (success) {
                reset();
            }
        }
    }

    public void prev() {
        if (stepper.getCurrentIndex() > 0) {
            stepper.previous();
        }
    }

    private RegistrationStep<HasProperty> getCurrentStep() {
        return (RegistrationStep<HasProperty>) this.tabs.getSelectionModel().getSelectedItem();
    }

    private boolean saveToDb() {
        try (DSLInstance dslInstance = new DSLInstance()) {
            StudentRepository studentRepo = new StudentRepositoryJooq(dslInstance.dsl);
            StudentFaceImageRepository studentFaceImageRepository = new StudentFaceImageRepositoryJooq(dslInstance.dsl);

            String studentId = studentRepo.create(
                    this.registerStudent.getStudentID().toString(),
                    this.registerStudent.getName(),
                    this.registerStudent.getEmail()
            );

            for (byte[] faceData : this.registerStudent.getFaceData().getFaceImages()) {
                studentFaceImageRepository.create(studentId, faceData);
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
        return false;
    }
}
