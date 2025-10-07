package g1t1.utils;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TextInputControl;

import java.util.Arrays;

public class BindingUtils {
    public static BooleanBinding allFilled(TextInputControl... inputs) {
        return Bindings.createBooleanBinding(
                () -> Arrays.stream(inputs).noneMatch(c -> c.getText().isEmpty()),
                Arrays.stream(inputs).map(TextInputControl::textProperty).toArray(Observable[]::new)
        );
    }
}
