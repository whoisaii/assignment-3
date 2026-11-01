package connectivity;

import graph.Edge;
import graph.Graph;
import graph.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class Kosaraju {
    public static List<List<Integer>> findComponents(Graph graph) {
        return findComponents(graph, new Metrics());
    }

    public static List<List<Integer>> findComponents(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC requires a directed graph");
        }
        int n = graph.getVertexCount();
        boolean[] visited = new boolean[n];
        int[] nextNeighbor = new int[n];
        List<Integer> finishOrder = new ArrayList<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        // A cursor remembers where each suspended DFS visit should continue.
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int vertex = stack.peek();
                List<Edge> neighbors = graph.getNeighbors(vertex);
                if (nextNeighbor[vertex] < neighbors.size()) {
                    metrics.edgeChecks++;
                    int to = neighbors.get(nextNeighbor[vertex]++).to;
                    if (!visited[to]) {
                        visited[to] = true;
                        stack.push(to);
                    }
                } else {
                    finishOrder.add(stack.pop());
                }
            }
        }

        Graph reversed = new Graph(n, true);
        for (Edge edge : graph.getEdges()) {
            metrics.edgeChecks++;
            reversed.addEdge(edge.to, edge.from, edge.weight);
        }
        visited = new boolean[n];
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int start = finishOrder.get(i);
            if (visited[start]) {
                continue;
            }
            List<Integer> component = new ArrayList<>();
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int vertex = stack.pop();
                component.add(vertex);
                for (Edge edge : reversed.getNeighbors(vertex)) {
                    metrics.edgeChecks++;
                    if (!visited[edge.to]) {
                        visited[edge.to] = true;
                        stack.push(edge.to);
                    }
                }
            }
            components.add(List.copyOf(component));
        }
        return List.copyOf(components);
    }
}
