package com.visualizer;

import com.visualizer.controller.MainMenuController;
import com.visualizer.database.DatabaseInterface;
import com.visualizer.database.MockDatabaseConnection;
import com.visualizer.database.SQLiteDatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {

    private Stage primaryStage;
    private TabPane mainTabPane;
    private DatabaseInterface db;

    // Flag to swap database implementation
    private static final boolean USE_MOCK_DATABASE = false;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Structurize - Data Structure Visualizer");

        // 1. Initialize Database
        initializeDatabase();

        // 2. Load the root layout (which is now a TabPane)
        FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/com/visualizer/fxml/RootLayout.fxml"));
        mainTabPane = rootLoader.load();

        // 3. Create the Scene and apply CSS
        Scene scene = new Scene(mainTabPane, 1000, 700);
        String cssPath = Objects.requireNonNull(getClass().getResource("/com/visualizer/css/styles.css")).toExternalForm();
        scene.getStylesheets().add(cssPath);

        primaryStage.setScene(scene);
        primaryStage.show();

        // 4. Load the Main Menu as the first, permanent tab
        showMainMenuTab();
    }

    private void initializeDatabase() {
        if (USE_MOCK_DATABASE) {
            db = new MockDatabaseConnection();
        } else {
            db = new SQLiteDatabaseConnection("data_structure_visualizer.db");
        }

        // Add a shutdown hook to close the database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (db != null) {
                db.close();
            }
        }));
    }

    /**
     * Loads the MainMenuView and adds it as a permanent tab.
     */
    public void showMainMenuTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/visualizer/fxml/MainMenuView.fxml"));
            Parent mainMenuRoot = loader.load();

            // Give the MainMenuController references to the TabPane and DB
            MainMenuController controller = loader.getController();
            controller.setReferences(mainTabPane, db);

            // Create the permanent Main Menu tab
            Tab mainMenuTab = new Tab("Main Menu");
            mainMenuTab.setContent(mainMenuRoot);
            mainMenuTab.setClosable(false); // Make it permanent

            mainTabPane.getTabs().add(mainMenuTab);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}