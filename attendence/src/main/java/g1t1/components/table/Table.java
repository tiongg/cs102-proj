package g1t1.components.table;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class Table extends StackPane {
    private static final int LABEL_WIDTH = 100;

    private final VBox tableContainer;

    private final HBox tableHeaderElement;
    private final VBox tableBodyElement;
    private Consumer<TableChipItem> onChipClick;

    public Table() {
        this.tableContainer = new VBox(0);
        VBox.setVgrow(this.tableContainer, Priority.ALWAYS);

        this.tableHeaderElement = new HBox(128);
        this.tableHeaderElement.setAlignment(Pos.CENTER);
        this.tableHeaderElement.getStyleClass().add("table-header");

        this.tableBodyElement = new VBox(12);
        this.tableBodyElement.getStyleClass().add("table-body");
        this.tableContainer.getChildren().addAll(tableHeaderElement, tableBodyElement);

        getChildren().add(tableContainer);
    }

    public void setTableHeaders(String... tableHeaders) {
        this.tableHeaderElement.getChildren().clear();

        for (String header : tableHeaders) {
            Label label = new Label(header);
            label.getStyleClass().add("table-header-label");
            label.setMinWidth(LABEL_WIDTH);
            label.setMaxWidth(LABEL_WIDTH);
            label.setAlignment(Pos.CENTER);
            tableHeaderElement.getChildren().add(label);
        }
    }

    public void createBody(List<? extends TableChipItem> chips) {
        this.tableBodyElement.getChildren().clear();

        for (TableChipItem chipData : chips) {
            HBox chip = new HBox(128);
            chip.getStyleClass().add("table-item");
            chip.setAlignment(Pos.CENTER);

            for (String data : chipData.getChipData()) {
                Label label = new Label(data);
                label.getStyleClass().add("table-item-label");
                label.setMinWidth(LABEL_WIDTH);
                label.setMaxWidth(LABEL_WIDTH);
                label.setAlignment(Pos.CENTER);
                chip.getChildren().add(label);
            }
            if (onChipClick != null) {
                chip.setCursor(Cursor.HAND);
                chip.setOnMouseClicked(e -> {
                    onChipClick.accept(chipData);
                });
            }
            this.tableBodyElement.getChildren().add(chip);
        }
    }

    public void setOnChipClick(Consumer<TableChipItem> handler) {
        this.onChipClick = handler;
    }
}
