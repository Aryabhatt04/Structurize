package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import com.visualizer.model.StackModel;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class StackController {

    @FXML private TextField valueTextField;
    @FXML private Pane visualizationPane;
    @FXML private Label explanationLabel;

    private StackModel model;
    private DatabaseInterface db;

    // List to keep track of visual nodes
    private final List<StackPane> visualNodes = new ArrayList<>();
    private static final double NODE_WIDTH = 100;
    private static final double NODE_HEIGHT = 40;
    private static final double NODE_GAP = 10;

    // This is called by MainMenuController AFTER the FXML is loaded
    public void initializeModel(int capacity, DatabaseInterface db) {
        this.model = new StackModel(capacity, db);
        this.db = db;
    }

    @FXML
    private void handlePush() {
        String value = valueTextField.getText();
        if (value.isEmpty()) {
            setExplanation("Error: Value cannot be empty", true);
            return;
        }

        try {
            model.push(value);
            setExplanation("Pushed " + value, false);
            addNodeToView(value);
            valueTextField.clear();
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handlePop() {
        try {
            String value = model.pop();
            setExplanation("Popped " + value, false);
            removeNodeFromView();
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        model.reset();
        setExplanation("Stack reset", false);
        visualNodes.clear();
        visualizationPane.getChildren().clear();
    }

    private void addNodeToView(String value) {
        StackPane node = createVisualNode(value);
        visualNodes.add(node);

        double startX = visualizationPane.getWidth() / 2 - NODE_WIDTH / 2;
        double startY = -NODE_HEIGHT; // Start above the pane
        double endY = visualizationPane.getHeight() - (visualNodes.size() * (NODE_HEIGHT + NODE_GAP));

        node.setLayoutX(startX);
        node.setLayoutY(startY);
        node.setOpacity(0);

        visualizationPane.getChildren().add(node);

        // Fade In
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1.0);
        ft.play();

        // Translate Down
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setToY(endY - startY); // Relative to current layoutY
        tt.play();
    }

    private void removeNodeFromView() {
        if (visualNodes.isEmpty()) return;

        StackPane node = visualNodes.remove(visualNodes.size() - 1);

        // Fade Out
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(0.0);

        // Translate Up
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setByY(-50); // Move up slightly

        ft.setOnFinished(e -> visualizationPane.getChildren().remove(node));
        tt.play();
        ft.play();
    }

    private StackPane createVisualNode(String value) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        stackPane.getStyleClass().add("data-node");
        stackPane.setAlignment(Pos.CENTER);

        Text text = new Text(value);
        text.getStyleClass().add("data-node-text");

        stackPane.getChildren().addAll(text);
        return stackPane;
    }

    private void setExplanation(String message, boolean isError) {
        explanationLabel.setText(message);
        if (isError) {
            explanationLabel.getStyleClass().add("error");
        } else {
            explanationLabel.getStyleClass().remove("error");
        }
    }
}