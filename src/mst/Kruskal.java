package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.Metrics;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static MSTResult findMST(Graph graph) {
        return findMST(graph, new Metrics());
    }

    public static MSTResult findMST(Graph graph, Metrics metrics) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal requires an undirected graph");
        }
        List<Edge> sorted = new ArrayList<>(graph.getEdges());
        sorted.sort((first, second) -> {
            metrics.comparisons++;
            return Integer.compare(first.weight, second.weight);
        });
        DSU dsu = new DSU(graph.getVertexCount(), metrics);
        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;
        for (Edge edge : sorted) {
            metrics.edgeChecks++;
            if (dsu.union(edge.from, edge.to)) {
                selected.add(edge);
                totalWeight += edge.weight;
                if (dsu.getComponents() == 1) {
                    break;
                }
            }
        }
        return new MSTResult(selected, totalWeight, dsu.getComponents());
    }
}
