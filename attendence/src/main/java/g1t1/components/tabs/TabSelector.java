package g1t1.components.tabs;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TabSelector extends HBox {
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private final ListProperty<String> labels =
            new SimpleListProperty<>(this, "labels", FXCollections.observableArrayList());
    private final ListProperty<Label> tabLabels = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IntegerProperty currentTabIndex = new SimpleIntegerProperty(0);

    public TabSelector() {
        getStyleClass().setAll("labeled-tab-selector");
        setSpacing(8);

        this.labels.addListener((ListChangeListener<String>) change -> {
            rebuildTabs();
        });

        this.currentTabIndex.addListener((obs, oldIndex, newIndex) -> {
            tabLabels.get(oldIndex.intValue()).pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
            tabLabels.get(newIndex.intValue()).pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
        });
    }

    @FXML()
    public void initialize() {
        rebuildTabs();
    }

    private void rebuildTabs() {
        tabLabels.clear();
        getChildren().clear();

        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            Label lblTabLabel = new Label(label);
            lblTabLabel.getStyleClass().add("labeled-tab-selector-label");
            final int index = i;
            lblTabLabel.setOnMouseClicked((e) -> {
                setCurrentTabIndex(index);
            });
            tabLabels.add(lblTabLabel);
            getChildren().add(lblTabLabel);
        }

        if (!tabLabels.isEmpty()) {
            tabLabels.getFirst().pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
        }
    }

    public IntegerProperty currentTabIndexProperty() {
        return currentTabIndex;
    }

    public int getCurrentTabIndex() {
        return currentTabIndexProperty().get();
    }

    public void setCurrentTabIndex(int index) {
        this.currentTabIndex.set(index);
    }

    public ObservableList<String> getLabels() {
        return labels.get();
    }

    public void setLabels(ObservableList<String> value) {
        labels.set(value);
    }

    public ListProperty<String> labelsProperty() {
        return labels;
    }
}
