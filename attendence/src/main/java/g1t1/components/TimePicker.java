package g1t1.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class TimePicker extends HBox {
    private final IntegerProperty hourProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty minuteProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty stepProperty = new SimpleIntegerProperty(5);

    @FXML
    private Label lblHour;

    @FXML
    private Label lblMinutes;

    public TimePicker() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TimePicker.fxml"));
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
        lblHour.textProperty().bind(hourProperty.asString("%02d"));
        lblMinutes.textProperty().bind(minuteProperty.asString("%02d"));
    }

    @FXML
    public void incrementHour() {
        int currentHour = this.getHourProperty().get() + 1;
        if (currentHour >= 24) {
            currentHour = 0;
        }
        this.getHourProperty().set(currentHour);
    }

    @FXML
    public void decrementHour() {
        int currentHour = this.getHourProperty().get() - 1;
        if (currentHour < 0) {
            currentHour = 23;
        }
        this.getHourProperty().set(currentHour);
    }

    @FXML
    public void incrementMinute() {
        int currentMinute = this.getMinuteProperty().get();
        int step = this.getStepProperty().get();
        if (currentMinute % step != 0) {
            currentMinute -= currentMinute % step;
        }
        currentMinute += step;
        if (currentMinute >= 60) {
            currentMinute = 0;
        }
        this.getMinuteProperty().set(currentMinute);
    }

    @FXML
    public void decrementMinute() {
        int currentMinute = this.getMinuteProperty().get();
        int step = this.getStepProperty().get();
        if (currentMinute % step != 0) {
            currentMinute -= currentMinute % step;
        } else {
            currentMinute -= step;
        }
        if (currentMinute < 0) {
            currentMinute = 60 - step;
        }
        this.getMinuteProperty().set(currentMinute);
    }

    public IntegerProperty getHourProperty() {
        return this.hourProperty;
    }

    public IntegerProperty getMinuteProperty() {
        return this.minuteProperty;
    }

    public IntegerProperty getStepProperty() {
        return this.stepProperty;
    }
}
