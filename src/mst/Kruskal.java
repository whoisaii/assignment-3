package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {
    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("Kruskal requires an undirected graph");
        }
        List<Edge> sorted = new ArrayList<>(graph.getEdges());
        sorted.sort(Comparator.comparingInt((Edge edge) -> edge.weight));
        DSU dsu = new DSU(graph.getVertexCount());
        List<Edge> selected = new ArrayList<>();
        long totalWeight = 0;
        for (Edge edge : sorted) {
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
