package mst;

import graph.Edge;
import graph.Graph;
import graph.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static MSTResult findMST(Graph graph) {
        return findMST(graph, new Metrics());
    }

    public static MSTResult findMST(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Prim requires an undirected graph");
        }
        boolean[] visited = new boolean[graph.getVertexCount()];
        PriorityQueue<Edge> queue = new PriorityQueue<>(
                (first, second) -> {
                    metrics.comparisons++;
                    return Integer.compare(first.weight, second.weight);
                });
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
            addEdges(graph, start, visited, queue, metrics);
            while (!queue.isEmpty()) {
                metrics.queuePolls++;
                Edge edge = queue.poll();
                if (visited[edge.to]) {
                    continue;
                }
                visited[edge.to] = true;
                selected.add(edge);
                totalWeight += edge.weight;
                addEdges(graph, edge.to, visited, queue, metrics);
            }
        }
        return new MSTResult(selected, totalWeight, components);
    }

    private static void addEdges(Graph graph, int vertex, boolean[] visited,
                                 PriorityQueue<Edge> queue, Metrics metrics) {
        for (Edge edge : graph.getNeighbors(vertex)) {
            metrics.edgeChecks++;
            if (!visited[edge.to]) {
                queue.offer(edge);
                metrics.queueOffers++;
            }
        }
    }
}
