package com.visualizer.database;

public interface DatabaseInterface extends AutoCloseable {
    /**
     * Executes a data manipulation statement (INSERT, UPDATE, CREATE).
     * @param sql The SQL query to execute.
     */
    void executeUpdate(String sql);

    /**
     * Closes the database connection.
     */
    @Override
    void close();
}