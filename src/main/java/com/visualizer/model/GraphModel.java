package com.visualizer.model;

import com.visualizer.database.DatabaseInterface;
import java.util.*;

public class GraphModel {

    // Node class
    public static class Node {
        public String id;
        public Node(String id) { this.id = id; }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            return id.equals(((Node) obj).id);
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    // Edge class
    public static class Edge {
        public Node source;
        public Node destination;
        public Edge(Node source, Node destination) {
            this.source = source;
            this.destination = destination;
        }
    }

    private final Map<Node, List<Node>> adjList = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final DatabaseInterface db;

    public GraphModel(DatabaseInterface db) {
        this.db = db;
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Graph', 'Create', NULL)");
    }

    public void addNode(String id) {
        Node newNode = new Node(id);
        if (adjList.containsKey(newNode)) {
            throw new IllegalArgumentException("Node " + id + " already exists.");
        }
        adjList.put(newNode, new ArrayList<>());
        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Graph', 'AddNode', '%s')", id.replace("'", "''")));
    }

    public void addEdge(String sourceId, String destId) {
        Node source = new Node(sourceId);
        Node dest = new Node(destId);

        if (!adjList.containsKey(source) || !adjList.containsKey(dest)) {
            throw new IllegalArgumentException("One or both nodes do not exist.");
        }

        adjList.get(source).add(dest);
        edges.add(new Edge(source, dest));
        // For undirected, add this: adjList.get(dest).add(source);

        db.executeUpdate(String.format("INSERT INTO operation_logs (structure, operation, value) VALUES ('Graph', 'AddEdge', '%s -> %s')", sourceId.replace("'", "''"), destId.replace("'", "''")));
    }

    public Map<Node, List<Node>> getAdjList() {
        return adjList;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Node> getNodes() {
        return adjList.keySet();
    }

    public Node getNodeById(String id) {
        for (Node n : adjList.keySet()) {
            if (n.id.equals(id)) return n;
        }
        return null;
    }

    public List<Node> bfs(String startNodeId) {
        Node startNode = getNodeById(startNodeId);
        if (startNode == null) throw new IllegalArgumentException("Start node not found.");

        List<Node> visitedOrder = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        queue.add(startNode);
        visited.add(startNode);

        while(!queue.isEmpty()) {
            Node current = queue.poll();
            visitedOrder.add(current);

            for (Node neighbor : adjList.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visitedOrder;
    }

    public List<Node> dfs(String startNodeId) {
        Node startNode = getNodeById(startNodeId);
        if (startNode == null) throw new IllegalArgumentException("Start node not found.");

        List<Node> visitedOrder = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();

        stack.push(startNode);

        while(!stack.isEmpty()) {
            Node current = stack.pop();

            if (!visited.contains(current)) {
                visited.add(current);
                visitedOrder.add(current);

                // Push neighbors in reverse order to visit them in correct order
                List<Node> neighbors = adjList.get(current);
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    if (!visited.contains(neighbors.get(i))) {
                        stack.push(neighbors.get(i));
                    }
                }
            }
        }
        return visitedOrder;
    }

    public void reset() {
        adjList.clear();
        edges.clear();
        db.executeUpdate("INSERT INTO operation_logs (structure, operation, value) VALUES ('Graph', 'Reset', NULL)");
    }
}