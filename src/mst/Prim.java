package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim requires an undirected graph");
        }
        boolean[] visited = new boolean[graph.getVertexCount()];
        PriorityQueue<Edge> queue = new PriorityQueue<>(
                Comparator.comparingInt((Edge edge) -> edge.weight));
        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        // Start again for any component that the previous traversal could not reach.
        for (int start = 0; start < graph.getVertexCount(); start++) {
            if (visited[start]) {
                continue;
            }
            components++;
            visited[start] = true;
            addEdges(graph, start, visited, queue);
            while (!queue.isEmpty()) {
                Edge edge = queue.poll();
                if (visited[edge.to]) {
                    continue;
                }
                visited[edge.to] = true;
                selected.add(edge);
                totalWeight += edge.weight;
                addEdges(graph, edge.to, visited, queue);
            }
        }
        return new MSTResult(selected, totalWeight, components);
    }

    private static void addEdges(Graph graph, int vertex, boolean[] visited,
                                 PriorityQueue<Edge> queue) {
        for (Edge edge : graph.getNeighbors(vertex)) {
            if (!visited[edge.to]) {
                queue.offer(edge);
            }
        }
    }
}