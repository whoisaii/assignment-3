package mst;

import dsu.DSU;
import graph.*;
import java.util.*;

public final class MinimumSpanningTree {
    private MinimumSpanningTree() {}
    public record Result(long weight, List<Graph.Edge> edges, int components) {
        public boolean connected() { return components == 1; }
    }
    private static void requireUndirected(Graph g) {
        if (g.directed()) throw new IllegalArgumentException("MST requires an undirected graph");
    }
    private static Comparator<Graph.Edge> comparator(Metrics m) {
        return (a, b) -> {
            m.comparisons++;
            int c = Integer.compare(a.weight(), b.weight());
            if (c == 0) c = Integer.compare(a.from(), b.from());
            if (c == 0) c = Integer.compare(a.to(), b.to());
            return c;
        };
    }
    public static Result kruskal(Graph g, Metrics m) {
        requireUndirected(g);
        List<Graph.Edge> sorted = new ArrayList<>(g.edges());
        sorted.sort(comparator(m));
        DSU dsu = new DSU(g.size(), m);
        List<Graph.Edge> chosen = new ArrayList<>();
        long total = 0; int components = g.size();
        for (Graph.Edge e : sorted) {
            m.edgeExaminations++;
            if (dsu.union(e.from(), e.to())) {
                chosen.add(e); total += e.weight(); components--;
                if (components == 1) break;
            }
        }
        return new Result(total, List.copyOf(chosen), components);
    }
    /** Lazy Prim. Restart at unseen vertices to handle disconnected graphs. */
    public static Result prim(Graph g, Metrics m) {
        requireUndirected(g);
        boolean[] seen = new boolean[g.size()];
        PriorityQueue<Graph.Edge> queue = new PriorityQueue<>(comparator(m));
        List<Graph.Edge> chosen = new ArrayList<>();
        long total = 0; int components = 0;
        for (int root = 0; root < g.size(); root++) {
            if (seen[root]) continue;
            components++; seen[root] = true; offer(g, root, seen, queue, m);
            while (!queue.isEmpty()) {
                Graph.Edge e = queue.poll(); m.queuePolls++;
                if (seen[e.to()]) continue;
                seen[e.to()] = true; chosen.add(e); total += e.weight();
                offer(g, e.to(), seen, queue, m);
            }
        }
        return new Result(total, List.copyOf(chosen), components);
    }
    private static void offer(Graph g, int v, boolean[] seen, PriorityQueue<Graph.Edge> q, Metrics m) {
        for (Graph.Edge e : g.neighbors(v)) {
            m.edgeExaminations++;
            if (!seen[e.to()]) { q.offer(e); m.queueOffers++; }
        }
    }
}
