package experiments;

import graph.Graph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public static Graph connectedUndirected(int vertices, int edges, long seed) {
        return generate(vertices, edges, seed, "undirected");
    }

    public static Graph dag(int vertices, int edges, long seed) {
        return generate(vertices, edges, seed, "dag");
    }

    public static Graph cyclicDirected(int vertices, int edges, long seed) {
        return generate(vertices, edges, seed, "cyclic");
    }

    public static Graph directed(int vertices, int edges, long seed) {
        return generate(vertices, edges, seed, "directed");
    }

    private static Graph generate(int n, int edges, long seed, String kind) {
        boolean undirected = kind.equals("undirected");
        boolean dag = kind.equals("dag");
        boolean cyclic = kind.equals("cyclic");
        long max = (long) n * (n - 1);
        if (undirected || dag) {
            max /= 2;
        }
        if (n < 1 || edges < 0 || edges > max
                || (undirected && edges < n - 1)
                || (cyclic && (n < 2 || edges < 2))) {
            throw new IllegalArgumentException("Impossible graph size");
        }
        Graph graph = new Graph(n, !undirected);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        if (undirected) {
            for (int v = 1; v < n; v++) {
                add(graph, used, random.nextInt(v), v, random, true);
            }
        }
        if (cyclic) {
            add(graph, used, 0, 1, random, false);
            add(graph, used, 1, 0, random, false);
        }
        while (used.size() < edges) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);
            if (from == to) {
                continue;
            }
            if ((undirected || dag) && from > to) {
                int temp = from;
                from = to;
                to = temp;
            }
            add(graph, used, from, to, random, undirected);
        }
        return graph;
    }

    private static void add(Graph graph, Set<Long> used, int from, int to,
                            Random random, boolean undirected) {
        if (undirected && from > to) {
            int temp = from;
            from = to;
            to = temp;
        }
        long key = ((long) from << 32) | (to & 0xffffffffL);
        if (used.add(key)) {
            graph.addEdge(from, to, 1 + random.nextInt(100));
        }
    }
}
