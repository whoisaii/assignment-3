package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;

    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 1) {
            throw new IllegalArgumentException("A graph must have at least one vertex");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        adjacency = new ArrayList<>();
        edges = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> getNeighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}

