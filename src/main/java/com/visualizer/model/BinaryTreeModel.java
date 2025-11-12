package com.visualizer.model;

import com.visualizer.database.DatabaseInterface;
import java.util.ArrayList;
import java.util.List;

public class BinaryTreeModel {

    // Inner class for the node
    public static class Node {
        public int value;
        public Node left;
        public Node right;

        public Node(int value) {
            this.value = value;
            left = null;
            right = null;
        }
    }

    private Node root;
    private final DatabaseInterface db;
    private final int capacity;
    private int size;

    public BinaryTreeModel(int capacity, DatabaseInterface db) {
        this.root = null;
        this.db = db;
        this.capacity = capacity;
        this.size = 0;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('BST', 'Create', 'Capacity " + capacity + "')");
    }

    public boolean isFull() {
        return size >= capacity;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public Node getRoot() {
        return root;
    }

    // --- Insert ---
    public void insert(int value) {
        if (isFull()) throw new IllegalStateException("Tree is full. Cannot insert " + value);

        root = insertRecursive(root, value);
        size++;
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('BST', 'Insert', '%d')", value));
    }

    private Node insertRecursive(Node current, int value) {
        if (current == null) {
            return new Node(value);
        }

        if (value < current.value) {
            current.left = insertRecursive(current.left, value);
        } else if (value > current.value) {
            current.right = insertRecursive(current.right, value);
        } else {
            // value already exists
            throw new IllegalArgumentException("Value " + value + " already exists in the tree.");
        }
        return current;
    }

    // --- Search ---
    public boolean search(int value) {
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('BST', 'Search', '%d')", value));
        return searchRecursive(root, value);
    }

    private boolean searchRecursive(Node current, int value) {
        if (current == null) {
            return false;
        }
        if (value == current.value) {
            return true;
        }
        return value < current.value
                ? searchRecursive(current.left, value)
                : searchRecursive(current.right, value);
    }

    // --- Traversal ---
    public List<Node> getInOrderTraversal() {
        List<Node> nodes = new ArrayList<>();
        inOrder(root, nodes);
        return nodes;
    }
    private void inOrder(Node node, List<Node> nodes) {
        if (node != null) {
            inOrder(node.left, nodes);
            nodes.add(node);
            inOrder(node.right, nodes);
        }
    }

    public List<Node> getPreOrderTraversal() {
        List<Node> nodes = new ArrayList<>();
        preOrder(root, nodes);
        return nodes;
    }
    private void preOrder(Node node, List<Node> nodes) {
        if (node != null) {
            nodes.add(node);
            preOrder(node.left, nodes);
            preOrder(node.right, nodes);
        }
    }

    public List<Node> getPostOrderTraversal() {
        List<Node> nodes = new ArrayList<>();
        postOrder(root, nodes);
        return nodes;
    }
    private void postOrder(Node node, List<Node> nodes) {
        if (node != null) {
            postOrder(node.left, nodes);
            postOrder(node.right, nodes);
            nodes.add(node);
        }
    }

    public void reset() {
        root = null;
        size = 0;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('BST', 'Reset', NULL)");
    }
}