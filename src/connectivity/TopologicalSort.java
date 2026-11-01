package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TopologicalSort {
    public static List<Integer> sort(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sorting requires a directed graph");
        }
        int n = graph.getVertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.getEdges()) {
            indegree[edge.to]++;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indegree[v] == 0) {
                queue.offer(v);
            }
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int vertex = queue.poll();
            order.add(vertex);
            for (Edge edge : graph.getNeighbors(vertex)) {
                indegree[edge.to]--;
                if (indegree[edge.to] == 0) {
                    queue.offer(edge.to);
                }
            }
        }
        if (order.size() != n) {
            throw new IllegalStateException("Graph contains a cycle");
        }
        return List.copyOf(order);
    }
}
