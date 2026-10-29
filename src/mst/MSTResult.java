package mst;

import graph.Edge;
import java.util.List;

public class MSTResult {
    public final List<Edge> edges;
    public final long totalWeight;
    public final int components;

    public MSTResult(List<Edge> edges, long totalWeight, int components) {
        this.edges = List.copyOf(edges);
        this.totalWeight = totalWeight;
        this.components = components;
    }

    public boolean isConnected() {
        return components == 1;
    }
}
