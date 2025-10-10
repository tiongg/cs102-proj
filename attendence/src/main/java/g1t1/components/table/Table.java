package g1t1.components.table;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.function.Consumer;

public class Table extends VBox {
    private HBox tableHeaderElement;
    private VBox tableBodyElement;
    private Consumer<TableChipItem> onChipClick;

    // FXMLLoader throwing tantrum because of how it is initialised
    public Table() {
        super(10);
    }

    public void setOnRowClick(Consumer<TableChipItem> handler) {
        this.onChipClick = handler;
    }

    public void setTableHeaders(String... tableHeaders) {
        tableHeaderElement = new HBox(100);
        tableHeaderElement.setAlignment(Pos.CENTER);
        tableHeaderElement.setStyle(
                "-fx-background-color: #838383ff; -fx-background-radius: 20; -fx-padding: 8; -fx-font-weight: bold;");

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
        if (tableBodyElement != null) {
            getChildren().remove(tableBodyElement);
        }
        this.tableBodyElement = new VBox(10);

        for (TableChipItem chipData : chips) {
            HBox chip = new HBox(100);
            chip.setAlignment(Pos.CENTER);
            chip.setStyle("-fx-background-color: #a4a4a4ff; -fx-background-radius: 20; -fx-padding: 4;");

            for (String data : chipData.getChipData()) {
                Label label = new Label(data);
                label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: Inter;");
                label.setMinWidth(80);
                label.setMaxWidth(80);
                label.setTextAlignment(TextAlignment.CENTER);
                chip.getChildren().add(label);
            }
            chip.setCursor(Cursor.HAND);
            chip.setOnMouseClicked(e -> {
                if (onChipClick != null)
                    onChipClick.accept(chipData);
            });
            tableBodyElement.getChildren().add(chip);
        }
        getChildren().add(tableBodyElement);
    }

    public void setOnChipClick(Consumer<TableChipItem> handler) {
        this.onChipClick = handler;
    }
}
