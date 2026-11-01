package connectivity;

import graph.Edge;
import graph.Graph;
import graph.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TopologicalSort {
    public static List<Integer> sort(Graph graph) {
        return sort(graph, new Metrics());
    }

    public static List<Integer> sort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sorting requires a directed graph");
        }
        int n = graph.getVertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.getEdges()) {
            metrics.edgeChecks++;
            indegree[edge.to]++;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                queue.offer(v);
                metrics.queueOffers++;
            }
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            metrics.queuePolls++;
            order.add(vertex);
            for (Edge edge : graph.getNeighbors(vertex)) {
                metrics.edgeChecks++;
                indegree[edge.to]--;
                if (indegree[edge.to] == 0) {
                    queue.offer(edge.to);
                    metrics.queueOffers++;
                }
            }
        }
        if (order.size() != n) {
            throw new IllegalStateException("Graph contains a cycle");
        }
        return List.copyOf(order);
    }
}
