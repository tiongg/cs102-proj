package g1t1.components.stepper;

import javafx.animation.FillTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class StepperSkin extends SkinBase<StepperControl> {
    private static final double PADDING_LEFT_RIGHT = 24;
    private static final double VERTICAL_PADDING = 8;
    private static final double OUTER_RADIUS = 8;
    private static final double CONNECTOR_STROKE = 4;

    private static final Color ACTIVE_COLOR = Color.web("#000000");
    private static final Color INACTIVE_COLOR = Color.web("#D9D9D9");

    private final Pane container = new Pane();

    // node lists for easy updates
    private final List<Circle> circles = new ArrayList<>();
    private final List<Line> connectors = new ArrayList<>();
    private final List<Label> labelNodes = new ArrayList<>();

    public StepperSkin(StepperControl control) {
        super(control);
        container.getStyleClass().add("stepper-container");
        getChildren().add(container);

        // build initial visuals
        rebuild();

        control.labelsProperty().addListener((ListChangeListener<String>) c -> rebuild());
        control.currentIndexProperty().addListener((obs, oldV, newV) -> updateVisuals());

        container.widthProperty().addListener((obs, oldV, newV) -> layoutNodes());
        container.heightProperty().addListener((obs, oldV, newV) -> layoutNodes());

        container.setPrefHeight(80);
    }

    private void rebuild() {
        circles.clear();
        connectors.clear();
        labelNodes.clear();
        container.getChildren().clear();

        StepperControl control = getSkinnable();
        List<String> labels = new ArrayList<>(control.getLabels() == null ? List.of() : control.getLabels());

        if (labels.isEmpty()) {
            // nothing to show
            return;
        }

        // create connectors first (n-1), then circles, then labels (ordered drawing)
        for (int i = 0; i < labels.size() - 1; i++) {
            Line line = new Line();
            line.getStyleClass().add("stepper-connector");
            line.setStrokeWidth(CONNECTOR_STROKE);
            connectors.add(line);
            container.getChildren().add(line);
        }

        for (String s : labels) {
            Circle circle = new Circle(OUTER_RADIUS);
            circle.getStyleClass().add("stepper-circle");
            circle.setFill(INACTIVE_COLOR);
            circle.setStrokeWidth(0);

            Label label = new Label(s);
            label.getStyleClass().add("stepper-label");
            label.setMinWidth(0);
            label.setAlignment(Pos.CENTER);

            circles.add(circle);
            labelNodes.add(label);

            // ensure circles and label are above connectors
            container.getChildren().addAll(circle, label);
        }

        // initial visuals & layout
        layoutNodes();
        updateVisuals();
    }

    private void layoutNodes() {
        double w = getSkinnable().getWidth();
        if (w <= 0) {
            w = Math.max(240, getSkinnable().prefWidth(-1));
        }

        int n = circles.size();
        if (n == 0) return;

        double usableW = Math.max(0, w - PADDING_LEFT_RIGHT * 2);
        double yForCircles = VERTICAL_PADDING + OUTER_RADIUS;
        double yForLabels = yForCircles + OUTER_RADIUS + 8;

        double spacing = (n == 1) ? 0 : usableW / (n - 1);

        List<Double> xs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double x = PADDING_LEFT_RIGHT + i * spacing;
            xs.add(x);
        }

        // layout connectors
        for (int i = 0; i < connectors.size(); i++) {
            Line line = connectors.get(i);
            double x1 = xs.get(i) + OUTER_RADIUS;
            double x2 = xs.get(i + 1) - OUTER_RADIUS;
            double y = yForCircles;
            line.setStartX(x1);
            line.setStartY(y);
            line.setEndX(x2);
            line.setEndY(y);
        }

        // layout circles and labels
        for (int i = 0; i < n; i++) {
            double x = xs.get(i);
            Circle outer = circles.get(i);
            Label label = labelNodes.get(i);

            outer.setCenterX(x);
            outer.setCenterY(yForCircles);

            double labelWidth = Math.max(20, label.prefWidth(-1));
            double lx = x - labelWidth / 2.0;
            double ly = yForLabels;
            label.resizeRelocate(lx, ly, labelWidth, label.prefHeight(-1));
        }
    }

    private void updateVisuals() {
        int n = circles.size();
        if (n == 0) return;
        int idx = Math.max(0, Math.min(getSkinnable().getCurrentIndex(), n - 1));

        for (int i = 0; i < n; i++) {
            boolean completedOrActive = i <= idx;
            circles.get(i).setFill(completedOrActive ? ACTIVE_COLOR : INACTIVE_COLOR);
            labelNodes.get(i).setTextFill(completedOrActive ? ACTIVE_COLOR : INACTIVE_COLOR);
        }

        for (int i = 0; i < connectors.size(); i++) {
            boolean filled = i < idx; // connectors up to previous step
            connectors.get(i).setStroke(filled ? ACTIVE_COLOR : INACTIVE_COLOR);
        }
    }

    // ensure container fills the skin area
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Math.max(240, getSkinnable().getLabels().size() * 80);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return OUTER_RADIUS * 2 + 40;
    }
}
