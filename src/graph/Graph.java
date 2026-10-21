package graph;

import java.util.*;

/** Vertices are 0..V-1. Undirected logical edges have two adjacency entries. */
public final class Graph {
    public record Edge(int from, int to, int weight) {}
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final boolean directed;
    public Graph(int vertices, boolean directed) {
        if (vertices < 1) throw new IllegalArgumentException("At least one vertex is required");
        this.directed = directed;
        adjacency = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }
    public int size() { return adjacency.size(); }
    public boolean directed() { return directed; }
    public void checkVertex(int v) {
        if (v < 0 || v >= size()) throw new IllegalArgumentException("Invalid vertex: " + v);
    }
    public Graph add(int from, int to, int weight) {
        checkVertex(from); checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge); adjacency.get(from).add(edge);
        if (!directed) adjacency.get(to).add(new Edge(to, from, weight));
        return this;
    }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> neighbors(int v) { checkVertex(v); return Collections.unmodifiableList(adjacency.get(v)); }
}
