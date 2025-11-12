package com.visualizer.database;

public class MockDatabaseConnection implements DatabaseInterface {

    public MockDatabaseConnection() {
        System.out.println("Mock Database Initialized (prints to console).");
    }

    @Override
    public void executeUpdate(String sql) {
        // Simply prints the SQL query to the console
        System.out.println("[MOCK DB]: " + sql);
    }

    @Override
    public void close() {
        System.out.println("Mock Database Closed.");
    }
}