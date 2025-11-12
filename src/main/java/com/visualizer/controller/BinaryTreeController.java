package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import com.visualizer.model.BinaryTreeModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryTreeController {

    @FXML private TextField valueTextField;
    @FXML private Pane visualizationPane;
    @FXML private Label explanationLabel;
    @FXML private HBox controlsBox; // To disable buttons during animation
    @FXML private Button insertButton;
    @FXML private Button searchButton;
    @FXML private Button resetButton;

    private BinaryTreeModel model;
    private DatabaseInterface db;
    private final Map<BinaryTreeModel.Node, Group> nodeMap = new HashMap<>();
    private static final double NODE_RADIUS = 20;
    private static final double V_GAP = 60;

    public void initializeModel(int capacity, DatabaseInterface db) {
        this.model = new BinaryTreeModel(capacity, db);
        this.db = db;
    }

    @FXML
    private void handleInsert() {
        try {
            int value = Integer.parseInt(valueTextField.getText());
            model.insert(value);
            setExplanation("Inserted " + value, false);
            valueTextField.clear();
            redrawTree(model.getRoot(), null);
        } catch (NumberFormatException e) {
            setExplanation("Error: Value must be an integer", true);
        } catch (IllegalStateException | IllegalArgumentException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleSearch() {
        try {
            int value = Integer.parseInt(valueTextField.getText());
            boolean found = model.search(value);
            setExplanation(found ? "Found " + value : "Did not find " + value, !found);
            animateSearch(value);
            valueTextField.clear();
        } catch (NumberFormatException e) {
            setExplanation("Error: Value must be an integer", true);
        }
    }

    @FXML
    private void handleInOrder() {
        animateTraversal(model.getInOrderTraversal(), "In-Order Traversal");
    }

    @FXML
    private void handlePreOrder() {
        animateTraversal(model.getPreOrderTraversal(), "Pre-Order Traversal");
    }

    @FXML
    private void handlePostOrder() {
        animateTraversal(model.getPostOrderTraversal(), "Post-Order Traversal");
    }

    @FXML
    private void handleReset() {
        model.reset();
        setExplanation("Binary Search Tree Reset", false);
        visualizationPane.getChildren().clear();
        nodeMap.clear();
    }

    private void redrawTree(BinaryTreeModel.Node root, Runnable onFinished) {
        visualizationPane.getChildren().clear();
        nodeMap.clear();
        if (root == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        // This is a complex problem. We'll use a simple algorithm for node positioning.
        // We'll calculate x-positions based on in-order traversal
        Map<BinaryTreeModel.Node, Double> xPos = new HashMap<>();
        calculatePositions(root, 0, visualizationPane.getWidth(), 0, xPos);

        drawNodeRecursive(root, 0, null, onFinished);
    }

    private void calculatePositions(BinaryTreeModel.Node node, double min, double max, int depth, Map<BinaryTreeModel.Node, Double> xPos) {
        if(node == null) return;

        // Simple positioning: divide space
        double x = (min + max) / 2;
        xPos.put(node, x);

        calculatePositions(node.left, min, x, depth + 1, xPos);
        calculatePositions(node.right, x, max, depth + 1, xPos);
    }

    private void drawNodeRecursive(BinaryTreeModel.Node node, int depth, Group parentVisual, Runnable onFinished) {
        if (node == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        double x = (visualizationPane.getWidth() / 2) + calculateNodeX(node, 0, (int)visualizationPane.getWidth());
        double y = (depth * V_GAP) + 50; // 50px top padding

        // Adjust x based on parent (this is a simplified algorithm)
        if(parentVisual != null) {
            if (node.value < Integer.parseInt(((Text)((StackPane)parentVisual.getChildren().get(0)).getChildren().get(0)).getText())) {
                x = parentVisual.getLayoutX() - (visualizationPane.getWidth() / (Math.pow(2, depth + 1)));
            } else {
                x = parentVisual.getLayoutX() + (visualizationPane.getWidth() / (Math.pow(2, depth + 1)));
            }
        } else {
            x = visualizationPane.getWidth() / 2;
        }

        Group nodeVisual = createVisualNode(node.value, x, y);
        nodeMap.put(node, nodeVisual);

        if (parentVisual != null) {
            Line line = new Line(parentVisual.getLayoutX(), parentVisual.getLayoutY() + NODE_RADIUS, x, y - NODE_RADIUS);
            line.getStyleClass().add("pointer-line");
            visualizationPane.getChildren().add(line);
        }

        visualizationPane.getChildren().add(nodeVisual);

        animateNodeAddition(nodeVisual, onFinished);

        drawNodeRecursive(node.left, depth + 1, nodeVisual, onFinished);
        drawNodeRecursive(node.right, depth + 1, nodeVisual, onFinished);
    }

    // This is a dummy function, proper X calculation is complex (e.g., Knuth's algorithm)
    // We'll use a simplified version
    private double calculateNodeX(BinaryTreeModel.Node node, int depth, int width) {
        // This won't work well. Let's try the recursive draw with parent position
        return 0;
    }

    private Group createVisualNode(int value, double x, double y) {
        Circle circle = new Circle(NODE_RADIUS);
        circle.getStyleClass().add("data-node-circle");

        Text text = new Text(String.valueOf(value));
        text.getStyleClass().add("data-node-text");

        StackPane stackPane = new StackPane(circle, text);
        stackPane.setAlignment(Pos.CENTER);

        Group group = new Group(stackPane);
        group.setLayoutX(x);
        group.setLayoutY(y);
        return group;
    }

    private void animateNodeAddition(Group nodeVisual, Runnable onFinished) {
        nodeVisual.setScaleX(0);
        nodeVisual.setScaleY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), nodeVisual);
        st.setToX(1);
        st.setToY(1);
        if (onFinished != null) {
            st.setOnFinished(e -> onFinished.run());
        }
        st.play();
    }

    private void setControlsDisabled(boolean disabled) {
        controlsBox.setDisable(disabled);
        insertButton.setDisable(disabled);
        searchButton.setDisable(disabled);
        resetButton.setDisable(disabled);
    }

    private void animateSearch(int value) {
        setControlsDisabled(true);
        SequentialTransition st = new SequentialTransition();
        BinaryTreeModel.Node current = model.getRoot();

        while (current != null) {
            Group visualNode = nodeMap.get(current);
            st.getChildren().add(createHighlightAnimation(visualNode, Color.web("#00aaff"))); // Search path

            if (value == current.value) {
                st.getChildren().add(createHighlightAnimation(visualNode, Color.web("#00b300"))); // Found
                current = null; // Stop
            } else if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        st.setOnFinished(e -> setControlsDisabled(false));
        st.play();
    }

    private void animateTraversal(List<BinaryTreeModel.Node> nodes, String description) {
        setControlsDisabled(true);
        SequentialTransition st = new SequentialTransition();
        StringBuilder traversalResult = new StringBuilder(description + ": ");

        for (BinaryTreeModel.Node node : nodes) {
            traversalResult.append(node.value).append(" ");
            Group visualNode = nodeMap.get(node);
            st.getChildren().add(createHighlightAnimation(visualNode, Color.web("#00aaff")));
        }

        st.setOnFinished(e -> {
            setControlsDisabled(false);
            setExplanation(traversalResult.toString(), false);
        });
        st.play();
    }

    private Animation createHighlightAnimation(Group visualNode, Color color) {
        StackPane sp = (StackPane) visualNode.getChildren().get(0);
        Circle circle = (Circle) sp.getChildren().get(0);

        FillTransition ft = new FillTransition(Duration.millis(300), circle);
        ft.setToValue(color);

        FillTransition ftReverse = new FillTransition(Duration.millis(300), circle);
        ftReverse.setToValue(Color.web("#007acc")); // Back to original color

        return new SequentialTransition(new PauseTransition(Duration.millis(100)), ft, new PauseTransition(Duration.millis(300)), ftReverse);
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