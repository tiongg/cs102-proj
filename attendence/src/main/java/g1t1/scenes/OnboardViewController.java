package g1t1.scenes;

import g1t1.components.Toast;
import g1t1.components.register.RegistrationStep;
import g1t1.components.stepper.StepperControl;
import g1t1.db.DSLInstance;
import g1t1.db.DbUtils;
import g1t1.db.enrollments.EnrollmentRepository;
import g1t1.db.enrollments.EnrollmentRepositoryJooq;
import g1t1.db.student_face_images.StudentFaceImageRepository;
import g1t1.db.student_face_images.StudentFaceImageRepositoryJooq;
import g1t1.db.students.StudentRepository;
import g1t1.db.students.StudentRepositoryJooq;
import g1t1.features.authentication.AuthenticationContext;
import g1t1.models.interfaces.HasProperty;
import g1t1.models.scenes.PageController;
import g1t1.models.users.RegisterStudent;
import g1t1.models.users.Student;
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
            AuthenticationContext.triggerUserUpdate();
            if (success) {
                Toast.show("Student added!", Toast.ToastType.SUCCESS);
                reset();
            } else {
                Toast.show("Error!", Toast.ToastType.ERROR);
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
            EnrollmentRepository enrollmentRepository = new EnrollmentRepositoryJooq(dslInstance.dsl);

            String studentId = studentRepo.create(
                    this.registerStudent.getStudentID().toString(),
                    this.registerStudent.getName(),
                    this.registerStudent.getEmail()
            );

            for (byte[] faceData : this.registerStudent.getFaceData().getFaceImages()) {
                studentFaceImageRepository.create(studentId, faceData);
            }

            String moduleId = this.registerStudent.getModuleSection().getId();
            enrollmentRepository.create(moduleId, studentId);

            // Update module section in memory
            Student student = DbUtils.getStudentById(studentId, this.registerStudent.getModuleSection());
            this.registerStudent.getModuleSection().addStudent(student);

            return true;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("Error during database operation: " + e.getMessage());
        }
        return false;
    }
}
