package com.visualizer.model;

import com.visualizer.database.DatabaseInterface;
import java.util.ArrayList;
import java.util.List;

public class LinkedListModel {

    // Inner class for the node
    public static class Node {
        public String value;
        public Node next;

        public Node(String value) {
            this.value = value;
            this.next = null;
        }
    }

    private Node head;
    private int size;
    private final DatabaseInterface db;
    private final int capacity;

    public LinkedListModel(int capacity, DatabaseInterface db) {
        this.head = null;
        this.size = 0;
        this.db = db;
        this.capacity = capacity;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'Create', 'Capacity " + capacity + "')");
    }

    public boolean isFull() {
        return size >= capacity;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int getSize() {
        return size;
    }

    public Node getHead() {
        return head;
    }

    public void addFirst(String value) {
        if (isFull()) throw new IllegalStateException("List is full. Cannot add " + value);

        Node newNode = new Node(value);
        newNode.next = head;
        head = newNode;
        size++;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'AddFirst', '%s')", value.replace("'", "''")));
    }

    public void addLast(String value) {
        if (isFull()) throw new IllegalStateException("List is full. Cannot add " + value);

        Node newNode = new Node(value);
        if (isEmpty()) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'AddLast', '%s')", value.replace("'", "''")));
    }

    public String removeFirst() {
        if (isEmpty()) throw new IllegalStateException("List is empty. Cannot remove.");

        String value = head.value;
        head = head.next;
        size--;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'RemoveFirst', '%s')", value.replace("'", "''")));
        return value;
    }

    public String removeLast() {
        if (isEmpty()) throw new IllegalStateException("List is empty. Cannot remove.");

        if (head.next == null) { // Only one element
            String value = head.value;
            head = null;
            size--;
            db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'RemoveLast', '%s')", value.replace("'", "''")));
            return value;
        }

        Node current = head;
        while (current.next.next != null) {
            current = current.next;
        }

        String value = current.next.value;
        current.next = null;
        size--;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'RemoveLast', '%s')", value.replace("'", "''")));
        return value;
    }

    public void reset() {
        head = null;
        size = 0;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('LinkedList', 'Reset', NULL)");
    }

    // Helper to get all node values, useful for controller
    public List<String> getAllValues() {
        List<String> values = new ArrayList<>();
        Node current = head;
        while(current != null) {
            values.add(current.value);
            current = current.next;
        }
        return values;
    }
}