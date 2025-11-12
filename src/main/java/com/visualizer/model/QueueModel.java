package com.visualizer.model;

import com.visualizer.database.DatabaseInterface;

public class QueueModel {
    private final int capacity;
    private final String[] queueArray;
    private int head;
    private int tail;
    private int size;
    private final DatabaseInterface db;

    public QueueModel(int capacity, DatabaseInterface db) {
        this.capacity = capacity;
        this.queueArray = new String[capacity];
        this.head = 0;
        this.tail = -1;
        this.size = 0;
        this.db = db;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Queue', 'Create', 'Capacity " + capacity + "')");
    }

    public boolean isFull() {
        return size == capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void enqueue(String value) {
        if (isFull()) {
            throw new IllegalStateException("Queue is full. Cannot enqueue " + value);
        }
        tail = (tail + 1) % capacity;
        queueArray[tail] = value;
        size++;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Queue', 'Enqueue', '%s')", value.replace("'", "''")));
    }

    public String dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty. Cannot dequeue.");
        }
        String value = queueArray[head];
        head = (head + 1) % capacity;
        size--;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Queue', 'Dequeue', '%s')", value.replace("'", "''")));
        return value;
    }

    public void reset() {
        head = 0;
        tail = -1;
        size = 0;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Queue', 'Reset', NULL)");
    }
}