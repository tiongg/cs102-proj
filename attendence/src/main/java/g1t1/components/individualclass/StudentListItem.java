package g1t1.components.individualclass;

import g1t1.models.users.Student;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StudentListItem extends HBox {
    public StudentListItem(Student s) {
        super();
        this.setSpacing(16);
        this.getStyleClass().add("student-list-item");

        VBox studentDetails = new VBox(8);
        Label lblName = new Label(s.getName());
        Label lblId = new Label(s.getId().toString());
        studentDetails.getChildren().addAll(lblName, lblId);

        this.getChildren().add(studentDetails);
    }
}
