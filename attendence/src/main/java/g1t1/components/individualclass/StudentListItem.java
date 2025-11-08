package g1t1.components.individualclass;

import g1t1.models.users.FaceData;
import g1t1.models.users.Student;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;

public class StudentListItem extends HBox {

    public StudentListItem(Student student) {
        super();
        this.setSpacing(16);
        this.setPadding(new Insets(12, 16, 12, 16));
        this.getStyleClass().add("student-list-item");
        this.setAlignment(Pos.CENTER_LEFT);

        // Avatar with face image or fallback
        StackPane avatar = createAvatar(student);

        // Student details (name, ID, email)
        VBox studentDetails = new VBox(4);
        HBox.setHgrow(studentDetails, Priority.ALWAYS);

        Label lblName = new Label(student.getName());
        lblName.getStyleClass().add("student-name");

        HBox idRow = new HBox(4);
        idRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon idIcon = new FontIcon("gmi-perm-identity");
        idIcon.setIconSize(12);
        idIcon.getStyleClass().add("student-info-icon");
        Label lblId = new Label(student.getId().toString());
        lblId.getStyleClass().add("student-id");
        idRow.getChildren().addAll(idIcon, lblId);

        HBox emailRow = new HBox(4);
        emailRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon emailIcon = new FontIcon("gmi-email");
        emailIcon.setIconSize(12);
        emailIcon.getStyleClass().add("student-info-icon");
        Label lblEmail = new Label(student.getEmail());
        lblEmail.getStyleClass().add("student-email");
        emailRow.getChildren().addAll(emailIcon, lblEmail);

        studentDetails.getChildren().addAll(lblName, idRow, emailRow);

        this.getChildren().addAll(avatar, studentDetails);
    }

    private static ImageView getImageView(byte[] imageData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        Image faceImage = new Image(bis);

        // Create circular ImageView
        ImageView imageView = new ImageView(faceImage);
        imageView.setFitWidth(56);
        imageView.setFitHeight(56);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        // Clip to circle
        Circle clip = new Circle(28, 28, 28);
        imageView.setClip(clip);
        return imageView;
    }

    private StackPane createAvatar(Student student) {
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(56, 56);
        avatarContainer.setMinSize(56, 56);
        avatarContainer.setMaxSize(56, 56);

        FaceData faceData = student.getFaceData();

        if (faceData != null && !faceData.getFaceImages().isEmpty()) {
            // Get first face image
            byte[] imageData = faceData.getFaceImages().getFirst();

            try {
                // Convert byte array to Image
                ImageView imageView = getImageView(imageData);

                avatarContainer.getChildren().add(imageView);
                avatarContainer.getStyleClass().add("avatar-image");

            } catch (Exception e) {
                // Fallback to initials if image fails to load
                System.err.println("Failed to load face image: " + e.getMessage());
                return createFallbackAvatar(student.getName());
            }
        } else {
            // Fallback to initials
            return createFallbackAvatar(student.getName());
        }

        return avatarContainer;
    }

    private StackPane createFallbackAvatar(String name) {
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(56, 56);
        avatarContainer.setMinSize(56, 56);
        avatarContainer.setMaxSize(56, 56);

        // Circle background
        Circle circle = new Circle(28);
        circle.getStyleClass().add("avatar-circle");

        // Initials
        Label initials = new Label(getInitials(name));
        initials.getStyleClass().add("avatar-initials");

        avatarContainer.getChildren().addAll(circle, initials);
        return avatarContainer;
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].charAt(0) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}