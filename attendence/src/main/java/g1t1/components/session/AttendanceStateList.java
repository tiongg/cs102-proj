package g1t1.components.session;

import g1t1.models.sessions.SessionAttendance;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// Display array of session attendances
public class AttendanceStateList extends VBox {
    public final ListProperty<SessionAttendance> attendances = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty editable = new SimpleBooleanProperty(false);

    @FXML
    private Label lblHeader;

    @FXML
    private VBox vbChips;

    public AttendanceStateList() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AttendanceStateList.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        lblHeader.textProperty().bind(titleProperty());

        // Rebuild children whenever list changes
        attendances.addListener((observable, oldList, newList) -> {
            updateChips();
        });

        // Also listen to list content changes (add/remove items)
        attendances.addListener((ListChangeListener<SessionAttendance>) change -> {
            updateChips();
        });
    }

    public String getTitle() {
        return this.titleProperty().get();
    }

    public void setTitle(String title) {
        this.titleProperty().set(title);
    }

    public StringProperty titleProperty() {
        return this.title;
    }

    public boolean getEditable() {
        return this.editableProperty().get();
    }

    public void setEditable(boolean editable) {
        this.editableProperty().set(editable);
    }

    public BooleanProperty editableProperty() {
        return this.editable;
    }

    private void updateChips() {
        vbChips.getChildren().clear();
        for (SessionAttendance attendance : attendances) {
            AttendanceChip chip = new AttendanceChip(attendance, this.getEditable());
            vbChips.getChildren().add(chip);
        }
    }
}
