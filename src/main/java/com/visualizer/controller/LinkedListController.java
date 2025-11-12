package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import com.visualizer.model.LinkedListModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class LinkedListController {

    @FXML private TextField valueTextField;
    @FXML private Pane visualizationPane;
    @FXML private Label explanationLabel;

    private LinkedListModel model;
    private DatabaseInterface db;
    private final List<Group> visualNodes = new ArrayList<>();

    private static final double NODE_WIDTH = 80;
    private static final double NODE_HEIGHT = 40;
    private static final double NODE_GAP = 60; // Larger gap for pointer

    public void initializeModel(int capacity, DatabaseInterface db) {
        this.model = new LinkedListModel(capacity, db);
        this.db = db;
    }

    @FXML
    private void handleAddFirst() {
        String value = valueTextField.getText();
        if (value.isEmpty()) {
            setExplanation("Error: Value cannot be empty", true);
            return;
        }
        try {
            model.addFirst(value);
            setExplanation("Added " + value + " to Head", false);
            valueTextField.clear();
            redrawList("addFirst");
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleAddLast() {
        String value = valueTextField.getText();
        if (value.isEmpty()) {
            setExplanation("Error: Value cannot be empty", true);
            return;
        }
        try {
            model.addLast(value);
            setExplanation("Added " + value + " to Tail", false);
            valueTextField.clear();
            redrawList("addLast");
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleRemoveFirst() {
        try {
            String value = model.removeFirst();
            setExplanation("Removed " + value + " from Head", false);
            redrawList("removeFirst");
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleRemoveLast() {
        try {
            String value = model.removeLast();
            setExplanation("Removed " + value + " from Tail", false);
            redrawList("removeLast");
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        model.reset();
        setExplanation("Linked List Reset", false);
        redrawList("reset");
    }

    private void redrawList(String operation) {
        List<String> values = model.getAllValues();
        double totalWidth = values.size() * (NODE_WIDTH + NODE_GAP) - NODE_GAP;
        if (totalWidth < 0) totalWidth = 0;

        double startX = (visualizationPane.getWidth() - totalWidth) / 2;
        double yPos = visualizationPane.getHeight() / 2 - NODE_HEIGHT / 2;

        if (operation.equals("reset")) {
            visualizationPane.getChildren().clear();
            visualNodes.clear();
            return;
        }

        // --- Animation Logic ---

        // 1. Handle removals
        if (operation.equals("removeFirst")) {
            if (!visualNodes.isEmpty()) {
                animateNodeRemoval(visualNodes.remove(0));
            }
        } else if (operation.equals("removeLast")) {
            if (!visualNodes.isEmpty()) {
                animateNodeRemoval(visualNodes.remove(visualNodes.size() - 1));
            }
        }

        // Clear all old pointers
        visualizationPane.getChildren().removeIf(node -> node instanceof Line);

        // 2. Animate existing nodes shifting
        SequentialTransition shiftTransition = new SequentialTransition();
        for (int i = 0; i < visualNodes.size(); i++) {
            Group node = visualNodes.get(i);

            int nodeIndex = i;
            if (operation.equals("addFirst")) {
                nodeIndex = i + 1; // The existing nodes are all shifted one to the right
            }
            // For other operations (addLast, removeFirst, removeLast), nodeIndex = i is correct.

            double targetX = startX + nodeIndex * (NODE_WIDTH + NODE_GAP);
            double targetY = yPos; // All nodes should be on the same yPos line

            TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
            tt.setToX(targetX - node.getLayoutX()); // Animate relative to current layoutX
            tt.setToY(targetY - node.getLayoutY()); // Animate relative to current layoutY

            // --- FIX ---
            // When the shift animation finishes, bake in the new position
            tt.setOnFinished(e -> {
                node.setLayoutX(targetX);
                node.setLayoutY(targetY);
                node.setTranslateX(0);
                node.setTranslateY(0);
            });
            // --- END FIX ---

            shiftTransition.getChildren().add(tt);
        }
        shiftTransition.play();

        // 3. Handle additions
        if (operation.equals("addFirst")) {
            Group newNode = createVisualNode(values.get(0));
            newNode.setLayoutX(startX);
            newNode.setLayoutY(yPos - 100); // Start above
            animateNodeAddition(newNode, startX, yPos);
            visualNodes.add(0, newNode);
        } else if (operation.equals("addLast")) {
            String newValue = values.get(values.size() - 1);
            Group newNode = createVisualNode(newValue);
            double targetX = startX + (values.size() - 1) * (NODE_WIDTH + NODE_GAP);
            newNode.setLayoutX(targetX);
            newNode.setLayoutY(yPos + 100); // Start below
            animateNodeAddition(newNode, targetX, yPos);
            visualNodes.add(newNode);
        }

        // 4. Redraw pointers after shift
        shiftTransition.setOnFinished(e -> drawPointers());
        if (shiftTransition.getChildren().isEmpty()) {
            drawPointers(); // Draw immediately if no shifting
        }
    }

    private void drawPointers() {
        visualizationPane.getChildren().removeIf(node -> node instanceof Line);
        for (int i = 0; i < visualNodes.size() - 1; i++) {
            Group startNode = visualNodes.get(i);
            Group endNode = visualNodes.get(i + 1);

            // Use layoutX/Y as they are now the "baked in" final positions
            double startX = startNode.getLayoutX() + NODE_WIDTH;
            double startY = startNode.getLayoutY() + NODE_HEIGHT / 2;
            double endX = endNode.getLayoutX();
            double endY = endNode.getLayoutY() + NODE_HEIGHT / 2;

            Line line = new Line(startX, startY, endX, endY);
            line.getStyleClass().add("pointer-line");

            // Add arrowhead
            double arrowHeadSize = 5.0;
            double dx = endX - startX;
            double dy = endY - startY;
            double angle = Math.atan2(dy, dx);

            double x1 = endX - arrowHeadSize * Math.cos(angle - Math.PI / 6);
            double y1 = endY - arrowHeadSize * Math.sin(angle - Math.PI / 6);
            double x2 = endX - arrowHeadSize * Math.cos(angle + Math.PI / 6);
            double y2 = endY - arrowHeadSize * Math.sin(angle + Math.PI / 6);

            Line arrow1 = new Line(endX, endY, x1, y1);
            Line arrow2 = new Line(endX, endY, x2, y2);
            arrow1.getStyleClass().add("pointer-line");
            arrow2.getStyleClass().add("pointer-line");

            visualizationPane.getChildren().addAll(line, arrow1, arrow2);
        }
    }

    private void animateNodeRemoval(Group node) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setByY(50); // Move down
        ft.setOnFinished(e -> visualizationPane.getChildren().remove(node));

        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.play();
    }

    private void animateNodeAddition(Group node, double targetX, double targetY) {
        node.setOpacity(0);
        visualizationPane.getChildren().add(node);

        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setToX(targetX - node.getLayoutX()); // Animates translateX
        tt.setToY(targetY - node.getLayoutY()); // Animates translateY

        ParallelTransition pt = new ParallelTransition(ft, tt);

        // --- FIX ---
        // When animation finishes, set the layout to the final position
        // and reset translation to 0. This "bakes in" the new position.
        pt.setOnFinished(e -> {
            node.setLayoutX(targetX);
            node.setLayoutY(targetY);
            node.setTranslateX(0);
            node.setTranslateY(0);
        });
        // --- END FIX ---

        pt.play();
    }

    private Group createVisualNode(String value) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        stackPane.getStyleClass().add("data-node");
        stackPane.setAlignment(Pos.CENTER);

        Text text = new Text(value);
        text.getStyleClass().add("data-node-text");

        stackPane.getChildren().add(text);

        // Group allows us to treat node and pointer as one
        return new Group(stackPane);
    }

    private void setExplanation(String message, boolean isError) {
        explanationLabel.setText(message);
        if (isError) {
            explanationLabel.getStyleClass().add("error");
            explanationLabel.getStyleClass().remove("success");
        } else {
            explanationLabel.getStyleClass().remove("error");
            explanationLabel.getStyleClass().add("success"); // Optional: add a 'success' class for green text
        }
    }
}