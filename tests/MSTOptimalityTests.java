package tests;

import experiments.GraphGenerator;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.Prim;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

public class MSTOptimalityTests {
    public static void main(String[] args) {
        for (int seed = 0; seed < 30; seed++) {
            Graph base = GraphGenerator.connectedUndirected(6, 9, seed);
            Graph graph = new Graph(6, false);
            Random random = new Random(seed);
            for (Edge edge : base.getEdges()) {
                graph.addEdge(edge.from, edge.to, random.nextInt(15) - 5);
            }
            long expected = minimumTreeWeight(graph);
            if (Kruskal.findMST(graph).totalWeight != expected
                    || Prim.findMST(graph).totalWeight != expected) {
                throw new AssertionError("MST is not optimal for seed " + seed);
            }
        }
        System.out.println("MST optimality checks passed for 30 graphs.");
    }

    private static long minimumTreeWeight(Graph graph) {
        List<Edge> edges = graph.getEdges();
        int n = graph.getVertexCount();
        long best = Long.MAX_VALUE;
        // Enumerate every V-1 edge subset; connectivity makes it a spanning tree.
        for (int mask = 0; mask < (1 << edges.size()); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            boolean[][] connected = new boolean[n][n];
            long weight = 0;
            for (int i = 0; i < edges.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    Edge edge = edges.get(i);
                    connected[edge.from][edge.to] = true;
                    connected[edge.to][edge.from] = true;
                    weight += edge.weight;
                }
            }
            boolean[] visited = new boolean[n];
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.offer(0);
            visited[0] = true;
            int count = 0;
            while (!queue.isEmpty()) {
                int vertex = queue.poll();
                count++;
                for (int to = 0; to < n; to++) {
                    if (connected[vertex][to] && !visited[to]) {
                        visited[to] = true;
                        queue.offer(to);
                    }
                }
            }
            if (count == n) {
                best = Math.min(best, weight);
            }
        }
        return best;
    }
}
