package app;

import graph.Graph;
import java.util.*;

public final class Generators {
    private Generators() {}
    public static Graph generate(int n, int count, long seed, String kind) {
        boolean undirected = kind.equals("undirected"), dag = kind.equals("dag");
        if (!Set.of("undirected", "dag", "cyclic", "routes").contains(kind))
            throw new IllegalArgumentException("Unknown graph kind");
        long max = (long)n * (n - 1) / (undirected || dag ? 2 : 1);
        int minimum = undirected ? n - 1 : kind.equals("cyclic") ? 2 : 0;
        if (n < 1 || count < minimum || count > max) throw new IllegalArgumentException("Impossible edge count");
        Graph g = new Graph(n, !undirected); Random r = new Random(seed); Set<Long> used = new HashSet<>();
        if (undirected) for (int v = 1; v < n; v++) add(g, used, r.nextInt(v), v, r.nextInt(100) + 1);
        if (kind.equals("cyclic")) {
            add(g, used, 0, 1, r.nextInt(100) + 1); add(g, used, 1, 0, r.nextInt(100) + 1);
        }
        while (g.edges().size() < count) {
            int a = r.nextInt(n), b = r.nextInt(n); if (a == b) continue;
            if ((undirected || dag) && a > b) { int t = a; a = b; b = t; }
            long key = (long)a * n + b;
            if (!used.contains(key)) add(g, used, a, b, r.nextInt(100) + 1);
        }
        return g;
    }
    private static void add(Graph g, Set<Long> used, int a, int b, int w) {
        used.add((long)a * g.size() + b); g.add(a, b, w);
    }
}
