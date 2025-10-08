package g1t1.components.table;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ClassesTable extends HBox {
    // FXMLLoader throwing tantrum
    public ClassesTable() {
        super(150);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #a4a4a4ff; -fx-background-radius: 20; -fx-padding: 4");
    }

    public ClassesTable(List<String> tableHeaders) {
        this();
        setClassesTable(tableHeaders);
    }

    public void setClassesTable(List<String> tableHeaders) {
        // clear prev values
        getChildren().clear();
        for (String header : tableHeaders) {
            Label label = new Label(header);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: Inter;");
            getChildren().add(label);
        }
    }

}
