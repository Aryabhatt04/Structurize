package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import com.visualizer.model.QueueModel;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.LinkedList;

public class QueueController {

    @FXML private TextField valueTextField;
    @FXML private Pane visualizationPane;
    @FXML private Label explanationLabel;

    private QueueModel model;
    private DatabaseInterface db;

    // Use LinkedList for easy add/remove at both ends
    private final LinkedList<StackPane> visualNodes = new LinkedList<>();
    private static final double NODE_WIDTH = 100;
    private static final double NODE_HEIGHT = 40;
    private static final double NODE_GAP = 10;

    // This is called by MainMenuController
    public void initializeModel(int capacity, DatabaseInterface db) {
        this.model = new QueueModel(capacity, db);
        this.db = db;
    }

    @FXML
    private void handleEnqueue() {
        String value = valueTextField.getText();
        if (value.isEmpty()) {
            setExplanation("Error: Value cannot be empty", true);
            return;
        }

        try {
            model.enqueue(value);
            setExplanation("Enqueued " + value, false);
            addNodeToView(value);
            valueTextField.clear();
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleDequeue() {
        try {
            String value = model.dequeue();
            setExplanation("Dequeued " + value, false);
            removeNodeFromView();
        } catch (IllegalStateException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        model.reset();
        setExplanation("Queue reset", false);
        visualNodes.clear();
        visualizationPane.getChildren().clear();
    }

    private double getStartX() {
        return (visualizationPane.getWidth() / 2) - (visualNodes.size() * (NODE_WIDTH + NODE_GAP)) / 2;
    }

    private void addNodeToView(String value) {
        StackPane node = createVisualNode(value);
        visualNodes.addLast(node);

        double startX = getStartX() + (visualNodes.size() - 1) * (NODE_WIDTH + NODE_GAP);
        double yPos = visualizationPane.getHeight() / 2 - NODE_HEIGHT / 2;

        node.setLayoutX(startX);
        node.setLayoutY(yPos);
        node.setOpacity(0); // Start invisible

        visualizationPane.getChildren().add(node);

        // Fade In
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setToValue(1.0);
        ft.play();

        // Re-center all other nodes
        repositionNodes();
    }

    private void removeNodeFromView() {
        if (visualNodes.isEmpty()) return;

        StackPane nodeToRemove = visualNodes.removeFirst();

        // Animate removal (fade out and move up)
        FadeTransition ft = new FadeTransition(Duration.millis(300), nodeToRemove);
        ft.setToValue(0.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), nodeToRemove);
        tt.setToY(nodeToRemove.getLayoutY() - 50);

        ft.setOnFinished(e -> visualizationPane.getChildren().remove(nodeToRemove));
        tt.play();
        ft.play();

        // Animate shifting for remaining nodes
        repositionNodes();
    }

    private void repositionNodes() {
        double startX = getStartX();

        SequentialTransition sequentialTransition = new SequentialTransition();

        for (int i = 0; i < visualNodes.size(); i++) {
            StackPane node = visualNodes.get(i);
            double targetX = startX + i * (NODE_WIDTH + NODE_GAP);

            TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
            tt.setToX(targetX - node.getLayoutX()); // This is relative to layoutX, but we want to set absolute X

            // A better way is to set absolute layoutX in an animation
            // Let's just animate the horizontal translation
            TranslateTransition shift = new TranslateTransition(Duration.millis(400), node);
            shift.setToX(targetX);
            shift.setToY(node.getLayoutY()); // Keep Y the same

            // This is tricky. Let's just animate the x-translation
            TranslateTransition ttMove = new TranslateTransition(Duration.millis(400), node);
            ttMove.setToX(targetX - node.getLayoutX() + node.getTranslateX()); // Move to new X position

            sequentialTransition.getChildren().add(ttMove);
        }

        // A simpler way (less animation, more robust)
        double newStartX = (visualizationPane.getWidth() / 2) - (visualNodes.size() * (NODE_WIDTH + NODE_GAP)) / 2;
        SequentialTransition st = new SequentialTransition();
        for(int i = 0; i < visualNodes.size(); i++) {
            StackPane node = visualNodes.get(i);
            double newX = newStartX + i * (NODE_WIDTH + NODE_GAP);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
            tt.setToX(newX - node.getLayoutX()); // This is relative, so it's newX - oldX
            st.getChildren().add(tt);
        }
        st.play();
    }

    private StackPane createVisualNode(String value) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        stackPane.getStyleClass().add("data-node");
        stackPane.setAlignment(Pos.CENTER);

        Text text = new Text(value);
        text.getStyleClass().add("data-node-text");

        stackPane.getChildren().add(text);
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