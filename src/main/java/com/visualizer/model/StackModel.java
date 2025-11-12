package com.visualizer.model;

import com.visualizer.database.DatabaseInterface;

public class StackModel {
    private final int capacity;
    private final String[] stackArray;
    private int top;
    private final DatabaseInterface db;

    public StackModel(int capacity, DatabaseInterface db) {
        this.capacity = capacity;
        this.stackArray = new String[capacity];
        this.top = -1;
        this.db = db;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Stack', 'Create', 'Capacity " + capacity + "')");
    }

    public boolean isFull() {
        return top == capacity - 1;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public void push(String value) {
        if (isFull()) {
            throw new IllegalStateException("Stack is full. Cannot push " + value);
        }
        stackArray[++top] = value;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Stack', 'Push', '%s')", value.replace("'", "''")));
    }

    public String pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty. Cannot pop.");
        }
        String value = stackArray[top--];
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Stack', 'Pop', '%s')", value.replace("'", "''")));
        return value;
    }

    public void reset() {
        top = -1;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Stack', 'Reset', NULL)");
    }
}