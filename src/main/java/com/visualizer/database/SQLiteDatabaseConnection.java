package com.visualizer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseConnection implements DatabaseInterface {

    private Connection conn;

    public SQLiteDatabaseConnection(String dbFilePath) {
        try {
            String url = "jdbc:sqlite:" + dbFilePath;
            conn = DriverManager.getConnection(url);
            System.out.println("Connected to SQLite database: " + dbFilePath);
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            conn = null;
        }
    }

    private void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS operation_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                structure TEXT NOT NULL,
                operation TEXT NOT NULL,
                value TEXT
            );
            """;
        executeUpdate(createTableSQL);
    }

    @Override
    public void executeUpdate(String sql) {
        if (conn == null) {
            System.err.println("Cannot execute update, database not connected.");
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("SQL execution error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("SQLite connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}