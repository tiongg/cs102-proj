package g1t1.components.stepper;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class StepperControl extends Control {
    private final ListProperty<String> labels =
            new SimpleListProperty<>(this, "labels", FXCollections.observableArrayList());

    private final IntegerProperty currentIndex =
            new SimpleIntegerProperty(this, "currentIndex", 0);

    public StepperControl() {
        getStyleClass().setAll("stepper-control");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new StepperSkin(this);
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

    public int getCurrentIndex() {
        return currentIndex.get();
    }

    public void setCurrentIndex(int idx) {
        if (idx < 0) idx = 0;
        if (getLabels() != null && idx >= getLabels().size()) idx = Math.max(0, getLabels().size() - 1);
        currentIndex.set(idx);
    }

    public IntegerProperty currentIndexProperty() {
        return currentIndex;
    }

    public void next() {
        setCurrentIndex(getCurrentIndex() + 1);
    }

    public void previous() {
        setCurrentIndex(getCurrentIndex() - 1);
    }

    public boolean isLast() {
        return currentIndexProperty().get() == labels.size() - 1;
    }
}