package dsu;

import graph.Metrics;

/** Iterative path compression and union by size avoid deep recursive find calls. */
public final class DSU {
    private final int[] parent, size;
    private final Metrics metrics;
    public DSU(int n, Metrics metrics) {
        if (n < 0) throw new IllegalArgumentException("Negative size");
        this.metrics = metrics; parent = new int[n]; size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }
    public int find(int x) {
        metrics.findCalls++;
        int root = x;
        while (root != parent[root]) { root = parent[root]; metrics.parentHops++; }
        while (x != root) {
            int next = parent[x]; parent[x] = root; x = next; metrics.parentHops++;
        }
        return root;
    }
    public boolean union(int a, int b) {
        metrics.unionCalls++;
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (size[ra] < size[rb]) { int tmp = ra; ra = rb; rb = tmp; }
        parent[rb] = ra; size[ra] += size[rb];
        return true;
    }
}
