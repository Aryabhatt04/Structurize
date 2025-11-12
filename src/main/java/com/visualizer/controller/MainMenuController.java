package com.visualizer.controller;

import com.visualizer.database.DatabaseInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.Optional;

public class MainMenuController {

    private DatabaseInterface db;
    private TabPane mainTabPane;

    /**
     * Called by MainApplication to give this controller the references it needs.
     */
    public void setReferences(TabPane mainTabPane, DatabaseInterface db) {
        this.mainTabPane = mainTabPane;
        this.db = db;
    }

    @FXML
    public void initialize() {
        // Initialization is now handled by setReferences()
    }

    @FXML
    private void handleStackClick() {
        promptForCapacity().ifPresent(capacity -> {
            addStructureTab("StackView.fxml", "Stack (Capacity: " + capacity + ")", capacity);
        });
    }

    @FXML
    private void handleQueueClick() {
        promptForCapacity().ifPresent(capacity -> {
            addStructureTab("QueueView.fxml", "Queue (Capacity: " + capacity + ")", capacity);
        });
    }

    @FXML
    private void handleLinkedListClick() {
        promptForCapacity().ifPresent(capacity -> {
            addStructureTab("LinkedListView.fxml", "Linked List (Capacity: " + capacity + ")", capacity);
        });
    }

    @FXML
    private void handleBstClick() {
        promptForCapacity().ifPresent(capacity -> {
            addStructureTab("BinaryTreeView.fxml", "BST (Capacity: " + capacity + ")", capacity);
        });
    }

    @FXML
    private void handleGraphClick() {
        promptForCapacity().ifPresent(capacity -> {
            addStructureTab("GraphView.fxml", "Graph (Capacity: " + capacity + ")", capacity);
        });
    }

    private Optional<Integer> promptForCapacity() {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Set Capacity");
        dialog.setHeaderText("Enter Data Structure Capacity");
        dialog.setContentText("Please enter capacity (1-20):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int capacity = Integer.parseInt(result.get());
                if (capacity >= 1 && capacity <= 20) {
                    return Optional.of(capacity);
                }
            } catch (NumberFormatException e) {
                // Invalid input, fall through to return empty
            }
        }
        return Optional.empty();
    }

    /**
     * Creates a new tab and adds it to the main TabPane.
     */
    private void addStructureTab(String fxmlFile, String title, int capacity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/visualizer/fxml/" + fxmlFile));
            Parent root = loader.load();

            // Get the controller and pass the capacity and db
            Object controller = loader.getController();
            if (controller instanceof StackController stackController) {
                stackController.initializeModel(capacity, db);
            } else if (controller instanceof QueueController queueController) {
                queueController.initializeModel(capacity, db);
            } else if (controller instanceof LinkedListController linkedListController) {
                linkedListController.initializeModel(capacity, db);
            } else if (controller instanceof BinaryTreeController binaryTreeController) {
                binaryTreeController.initializeModel(capacity, db);
            } else if (controller instanceof GraphController graphController) {
                graphController.initializeModel(capacity, db);
            }

            Tab tab = new Tab(title);
            tab.setContent(root);
            tab.setClosable(true); // Make the new structure tab closable

            // Add the new tab and select it
            mainTabPane.getTabs().add(tab);
            mainTabPane.getSelectionModel().select(tab);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}