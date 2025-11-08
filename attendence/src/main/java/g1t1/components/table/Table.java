package g1t1.components.table;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class Table extends StackPane {
    private static final int LABEL_WIDTH = 100;

    private final HBox tableHeaderElement;
    private final VBox tableBodyElement;
    private final ListProperty<TableChipItem> chipItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Integer> sortOrders = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final List<FontIcon> headerIcons = new ArrayList<>();
    private final StringProperty emptyMessage = new SimpleStringProperty("Empty!");
    private final StringProperty emptyMessageDescription = new SimpleStringProperty();
    private Consumer<TableChipItem> onChipClick;

    public Table() {
        VBox tableContainer = new VBox(0);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        this.tableHeaderElement = new HBox();
        this.tableHeaderElement.setAlignment(Pos.CENTER);
        this.tableHeaderElement.getStyleClass().add("table-header");

        this.tableBodyElement = new VBox(12);
        this.tableBodyElement.getStyleClass().add("table-body");
        VBox.setVgrow(this.tableBodyElement, Priority.ALWAYS);

        tableContainer.getChildren().addAll(tableHeaderElement, tableBodyElement);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        getChildren().add(tableContainer);
        chipItems.addListener((ob, oldList, newList) -> {
            updateTableBody();
        });

        chipItems.addListener((ListChangeListener<TableChipItem>) change -> {
            updateTableBody();
        });

        sortOrders.addListener((ListChangeListener<Integer>) change -> {
            updateHeaderIcons();
        });
    }

    public void setTableHeaders(String... tableHeaders) {
        this.tableHeaderElement.getChildren().clear();
        this.headerIcons.clear();
        this.sortOrders.clear();

        for (int index = 0; index < tableHeaders.length; index++) {
            HBox headerItem = new HBox(12);
            headerItem.setAlignment(Pos.CENTER);
            HBox.setHgrow(headerItem, Priority.ALWAYS);

            Label label = new Label(tableHeaders[index]);
            label.getStyleClass().add("table-header-label");
            label.setMinWidth(LABEL_WIDTH);
            label.setMaxWidth(LABEL_WIDTH);
            label.setAlignment(Pos.CENTER);

            FontIcon icon = new FontIcon();
            icon.setIconLiteral("ion4-md-arrow-round-up");
            icon.setVisible(false);
            headerIcons.add(icon);

            // Not sorting based on this
            sortOrders.add(0);
            final int i = index;
            headerItem.setOnMouseClicked((e) -> {
                int prevSortOrder = sortOrders.get(i);
                Collections.fill(sortOrders, 0);
                int newSortOrder = prevSortOrder < 1 ? 1 : -1;
                sortOrders.set(i, newSortOrder);
                this.chipItems.sort(Comparator.comparingLong(x -> x.getComparatorKeys()[i] * newSortOrder));
            });

            headerItem.getChildren().setAll(label, icon);
            tableHeaderElement.getChildren().add(headerItem);
        }
    }

    public void setTableBody(List<? extends TableChipItem> chips) {
        chipItems.setAll(chips);
        updateTableBody();
    }

    public ListProperty<TableChipItem> getChipItems() {
        return chipItems;
    }

    public void setOnChipClick(Consumer<TableChipItem> handler) {
        this.onChipClick = handler;
    }

    private void updateTableBody() {
        this.tableBodyElement.getChildren().clear();

        if (chipItems.size() <= 0) {
            StackPane emptyStack = new StackPane();
            emptyStack.setAlignment(Pos.CENTER);
            VBox.setVgrow(emptyStack, Priority.ALWAYS);

            VBox emptyContainer = new VBox();
            emptyContainer.setAlignment(Pos.CENTER);

            FontIcon icon = new FontIcon();
            icon.setIconLiteral("gmi-inbox");
            icon.setIconSize(64);
            icon.setIconColor(Color.LIGHTGRAY);

            Label lblEmpty = new Label(getEmptyMessage());
            Label lblSecondary = new Label(getEmptyMessageDescription());
            lblSecondary.getStyleClass().add("empty-state-text");

            emptyContainer.getChildren().addAll(icon, lblEmpty, lblSecondary);
            emptyStack.getChildren().add(emptyContainer);
            this.tableBodyElement.getChildren().add(emptyStack);

            return;
        }

        for (TableChipItem chipData : chipItems) {
            HBox chip = new HBox();
            chip.getStyleClass().add("table-item");
            chip.setAlignment(Pos.CENTER);

            for (String data : chipData.getChipData()) {
                HBox item = new HBox(12);
                item.setAlignment(Pos.CENTER);
                HBox.setHgrow(item, Priority.ALWAYS);

                // Dummy icon for alignment
                FontIcon icon = new FontIcon();
                icon.setIconLiteral("ion4-md-arrow-round-up");
                icon.setVisible(false);

                Label label = new Label(data);
                label.getStyleClass().add("table-item-label");
                label.setAlignment(Pos.CENTER);
                label.setMinWidth(LABEL_WIDTH);
                label.setMaxWidth(LABEL_WIDTH);

                item.getChildren().addAll(label, icon);
                chip.getChildren().add(item);
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

    private void updateHeaderIcons() {
        for (int i = 0; i < headerIcons.size() && i < sortOrders.size(); i++) {
            FontIcon icon = headerIcons.get(i);
            int sortOrder = sortOrders.get(i);

            if (sortOrder == 0) {
                // Not sorting - hide icon
                icon.setVisible(false);
            } else if (sortOrder > 0) {
                icon.setVisible(true);
                icon.setIconLiteral("ion4-md-arrow-round-up");
            } else {
                icon.setVisible(true);
                icon.setIconLiteral("ion4-md-arrow-round-down");
            }
        }
    }

    public StringProperty emptyMessageProperty() {
        return this.emptyMessage;
    }

    public String getEmptyMessage() {
        return this.emptyMessageProperty().get();
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessageProperty().set(emptyMessage);
    }

    public StringProperty emptyMessageDescriptionProperty() {
        return this.emptyMessageDescription;
    }

    public String getEmptyMessageDescription() {
        return this.emptyMessageDescriptionProperty().get();
    }

    public void setEmptyMessageDescription(String emptyMessageDescription) {
        this.emptyMessageDescriptionProperty().set(emptyMessageDescription);
    }
}
