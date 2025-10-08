package g1t1.components;

import g1t1.App;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {
    /**
     * Displays a temporary popup message over the given stage.
     *
     * @param message the text to display
     */
    public static void show(String message) {
        show(message, 1000, ToastType.DEFAULT);
    }

    public static void show(String message, int durationMs) {
        show(message, durationMs, ToastType.DEFAULT);
    }

    public static void show(String message, ToastType severity) {
        show(message, 1000, severity);
    }

    /**
     * Displays a temporary popup message with a custom duration.
     *
     * @param message    the text to display
     * @param durationMs duration in milliseconds
     */
    public static void show(String message, int durationMs, ToastType severity) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            Label label = new Label(message);
            label.getStyleClass().add("toast-label");
            label.setWrapText(true);

            StackPane root = new StackPane(label);
            root.setAlignment(Pos.CENTER);
            root.getStyleClass().add("toast");
            String stateClass = switch (severity) {
                case ERROR -> "error";
                case SUCCESS -> "success";
                case WARNING -> "warning";
                default -> null;
            };

            if (stateClass != null) {
                root.getStyleClass().add(stateClass);
            }

            popup.getContent().add(root);

            // Show at bottom center of window
            Stage ownerStage = App.getRootStage();
            Scene scene = ownerStage.getScene();
            double x = ownerStage.getX() + (scene.getWidth() - root.getWidth()) / 2;
            double y = ownerStage.getY() + scene.getHeight() - 80;
            popup.show(ownerStage, x, y);

            // Fade in/out
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.millis(durationMs));

            fadeOut.setOnFinished(e -> popup.hide());
            fadeOut.play();
        });
    }

    public enum ToastType {
        DEFAULT, SUCCESS, WARNING, ERROR
    }
}
