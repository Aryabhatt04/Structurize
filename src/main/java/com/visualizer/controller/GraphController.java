package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import com.visualizer.model.GraphModel;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GraphController {

    @FXML private TextField nodeTextField;
    @FXML private TextField edgeFromTextField;
    @FXML private TextField edgeToTextField;
    @FXML private TextField startNodeTextField;
    @FXML private Pane visualizationPane;
    @FXML private Label explanationLabel;
    @FXML private VBox controlsBox;

    private GraphModel model;
    private DatabaseInterface db;

    private final Map<GraphModel.Node, Group> nodeMap = new HashMap<>();
    private final Map<GraphModel.Edge, Line> edgeMap = new HashMap<>();
    private static final double NODE_RADIUS = 20;

    // For dragging
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    public void initializeModel(int capacity, DatabaseInterface db) {
        // Capacity is less relevant for graph, but we follow the pattern
        this.model = new GraphModel(db);
        this.db = db;
    }

    @FXML
    private void handleAddNode() {
        String id = nodeTextField.getText();
        if (id.isEmpty()) {
            setExplanation("Error: Node ID cannot be empty", true);
            return;
        }
        try {
            model.addNode(id);
            setExplanation("Added Node " + id, false);
            drawNewNode(model.getNodeById(id));
            nodeTextField.clear();
        } catch (IllegalArgumentException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleAddEdge() {
        String from = edgeFromTextField.getText();
        String to = edgeToTextField.getText();
        if (from.isEmpty() || to.isEmpty()) {
            setExplanation("Error: Edge fields cannot be empty", true);
            return;
        }
        try {
            model.addEdge(from, to);
            setExplanation("Added Edge " + from + " -> " + to, false);
            drawNewEdge(model.getNodeById(from), model.getNodeById(to));
            edgeFromTextField.clear();
            edgeToTextField.clear();
        } catch (IllegalArgumentException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBFS() {
        String startId = startNodeTextField.getText();
        try {
            List<GraphModel.Node> traversal = model.bfs(startId);
            animateTraversal(traversal, "BFS");
        } catch (IllegalArgumentException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleDFS() {
        String startId = startNodeTextField.getText();
        try {
            List<GraphModel.Node> traversal = model.dfs(startId);
            animateTraversal(traversal, "DFS");
        } catch (IllegalArgumentException e) {
            setExplanation(e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        model.reset();
        visualizationPane.getChildren().clear();
        nodeMap.clear();
        edgeMap.clear();
        setExplanation("Graph Reset", false);
    }

    private void drawNewNode(GraphModel.Node node) {
        Random rand = new Random();
        double x = rand.nextDouble() * (visualizationPane.getWidth() - 40) + 20;
        double y = rand.nextDouble() * (visualizationPane.getHeight() - 40) + 20;

        Group nodeVisual = createVisualNode(node.id, x, y);
        nodeMap.put(node, nodeVisual);
        visualizationPane.getChildren().add(nodeVisual);

        // Make draggable
        nodeVisual.setOnMousePressed(e -> {
            orgSceneX = e.getSceneX();
            orgSceneY = e.getSceneY();
            orgTranslateX = nodeVisual.getTranslateX();
            orgTranslateY = nodeVisual.getTranslateY();
            nodeVisual.toFront();
        });

        nodeVisual.setOnMouseDragged(e -> {
            double offsetX = e.getSceneX() - orgSceneX;
            double offsetY = e.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            nodeVisual.setTranslateX(newTranslateX);
            nodeVisual.setTranslateY(newTranslateY);

            // Update connected edges
            updateEdges(node);
        });
    }

    private void drawNewEdge(GraphModel.Node from, GraphModel.Node to) {
        Group fromVisual = nodeMap.get(from);
        Group toVisual = nodeMap.get(to);

        Line line = new Line();
        line.getStyleClass().add("pointer-line");
        line.setStartX(fromVisual.getLayoutX() + fromVisual.getTranslateX());
        line.setStartY(fromVisual.getLayoutY() + fromVisual.getTranslateY());
        line.setEndX(toVisual.getLayoutX() + toVisual.getTranslateX());
        line.setEndY(toVisual.getLayoutY() + toVisual.getTranslateY());

        visualizationPane.getChildren().add(line);
        line.toBack(); // Send behind nodes

        edgeMap.put(new GraphModel.Edge(from, to), line);
    }

    private void updateEdges(GraphModel.Node node) {
        for (GraphModel.Edge edge : model.getEdges()) {
            Line line = edgeMap.get(edge);
            if (line == null) continue;

            Group fromVisual = nodeMap.get(edge.source);
            Group toVisual = nodeMap.get(edge.destination);

            if (edge.source.equals(node)) {
                line.setStartX(fromVisual.getLayoutX() + fromVisual.getTranslateX());
                line.setStartY(fromVisual.getLayoutY() + fromVisual.getTranslateY());
            }
            if (edge.destination.equals(node)) {
                line.setEndX(toVisual.getLayoutX() + toVisual.getTranslateX());
                line.setEndY(toVisual.getLayoutY() + toVisual.getTranslateY());
            }
        }
    }

    private Group createVisualNode(String id, double x, double y) {
        Circle circle = new Circle(NODE_RADIUS);
        circle.getStyleClass().add("data-node-circle");

        Text text = new Text(id);
        text.getStyleClass().add("data-node-text");

        StackPane stackPane = new StackPane(circle, text);
        stackPane.setAlignment(Pos.CENTER);

        Group group = new Group(stackPane);
        group.setLayoutX(x);
        group.setLayoutY(y);
        group.getStyleClass().add("graph-node"); // For cursor
        return group;
    }

    private void animateTraversal(List<GraphModel.Node> nodes, String description) {
        controlsBox.setDisable(true);
        SequentialTransition st = new SequentialTransition();
        StringBuilder traversalResult = new StringBuilder(description + ": ");

        for (GraphModel.Node node : nodes) {
            traversalResult.append(node.id).append(" ");
            Group visualNode = nodeMap.get(node);
            st.getChildren().add(createHighlightAnimation(visualNode, Color.web("#00aaff")));
        }

        st.setOnFinished(e -> {
            controlsBox.setDisable(false);
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