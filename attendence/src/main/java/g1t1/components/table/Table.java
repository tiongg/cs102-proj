package g1t1.components.table;

import java.util.List;

import javax.swing.GroupLayout.Alignment;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class Table extends VBox {
    private HBox tableHeaderElement;
    private VBox tableBodyElement;

    // FXMLLoader throwing tantrum because of how it is initialised
    public Table() {
        super(10);
    }

    public void setTable(List<String> tableHeaders) {
        tableHeaderElement = new HBox(150);
        tableHeaderElement.setAlignment(Pos.CENTER);
        tableHeaderElement.setStyle("-fx-background-color: #a4a4a4ff; -fx-background-radius: 20; -fx-padding: 4");

        // clear prev values
        getChildren().clear();
        for (String header : tableHeaders) {
            Label label = new Label(header);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: Inter;");
            label.setMinWidth(80);
            label.setMaxWidth(80);
            label.setTextAlignment(TextAlignment.CENTER);
            tableHeaderElement.getChildren().add(label);
        }

        getChildren().add(tableHeaderElement);
    }

    public void createBody(List<? extends TableChipItem> chips) {
        this.tableBodyElement = new VBox(10);

        for (TableChipItem chipData : chips) {
            HBox chip = new HBox(150);
            chip.setAlignment(Pos.CENTER);
            chip.setStyle("-fx-background-color: #a4a4a4ff; -fx-background-radius: 20; -fx-padding: 4");

            for (String data : chipData.getChipData()) {
                Label label = new Label(data);
                label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: Inter;");
                label.setMinWidth(80);
                label.setMaxWidth(80);
                label.setTextAlignment(TextAlignment.CENTER);
                chip.getChildren().add(label);
            }
            tableBodyElement.getChildren().add(chip);
        }
        getChildren().add(tableBodyElement);
    }

    // might consist of different types in ChipsTable
    // public void bindTo(ChipsTable<?> table){

    // }

}
