package tests;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;
import java.util.List;
import java.util.Random;

public class DirectedTests {
    private static int checks;

    public static void main(String[] args) {
        testTopology();
        testComponents();
        testPaths();
        testRandomGraphs();
        testDeepChain();
        testInvalidInput();
        System.out.println("All " + checks + " directed checks passed.");
    }

    private static void testTopology() {
        Graph dag = new Graph(5, true);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 2, 1);
        dag.addEdge(2, 3, 1);
        verifyOrder(dag);
        dag.addEdge(3, 0, 1);
        expectCycle(dag);
        Graph loop = new Graph(1, true);
        loop.addEdge(0, 0, 0);
        expectCycle(loop);
        verifyOrder(new Graph(1, true));
    }

    private static void verifyOrder(Graph graph) {
        List<Integer> order = TopologicalSort.sort(graph);
        int n = graph.getVertexCount();
        check(order.size() == n, "Order covers graph");
        int[] position = new int[n];
        boolean[] seen = new boolean[n];
        for (int i = 0; i < n; i++) {
            int vertex = order.get(i);
            check(!seen[vertex], "No repeated vertex in order");
            seen[vertex] = true;
            position[vertex] = i;
        }
        for (Edge edge : graph.getEdges()) {
            check(position[edge.from] < position[edge.to], "Edge points forward");
        }
    }

    private static void expectCycle(Graph graph) {
        boolean detected = false;
        try {
            TopologicalSort.sort(graph);
        } catch (IllegalStateException exception) {
            detected = true;
        }
        check(detected, "Cycle detected");
    }

    private static void testComponents() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        check(Kosaraju.findComponents(graph).size() == 3, "Three SCCs");
        verifySCC(graph);
        verifySCC(new Graph(1, true));
        verifySCC(new Graph(4, true));
    }

    private static void verifySCC(Graph graph) {
        int n = graph.getVertexCount();
        boolean[][] reachable = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            reachable[i][i] = true;
        }
        for (Edge edge : graph.getEdges()) {
            reachable[edge.from][edge.to] = true;
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    reachable[i][j] |= reachable[i][k] && reachable[k][j];
                }
            }
        }
        int[] componentOf = new int[n];
        boolean[] seen = new boolean[n];
        List<List<Integer>> components = Kosaraju.findComponents(graph);
        for (int c = 0; c < components.size(); c++) {
            check(!components.get(c).isEmpty(), "Nonempty component");
            for (int vertex : components.get(c)) {
                check(!seen[vertex], "Vertex occurs in only one SCC");
                seen[vertex] = true;
                componentOf[vertex] = c;
            }
        }
        for (int i = 0; i < n; i++) {
            check(seen[i], "SCC coverage");
            for (int j = 0; j < n; j++) {
                check((componentOf[i] == componentOf[j]) ==
                        (reachable[i][j] && reachable[j][i]), "Mutual reachability");
            }
        }
    }

    private static void testPaths() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 10);
        graph.addEdge(1, 2, 3);
        ShortestPathResult result = Dijkstra.findPaths(graph, 0);
        check(result.getDistance(2) == 7, "Known shortest distance");
        check(result.getPath(2).equals(List.of(0, 1, 2)), "Known path");
        check(result.getDistance(3) == Long.MAX_VALUE, "Unreachable distance");
        check(result.getPath(3).isEmpty(), "Unreachable path");
        check(result.getPath(0).equals(List.of(0)), "Source path");
        Graph zeros = new Graph(3, true);
        zeros.addEdge(0, 1, 0);
        zeros.addEdge(1, 0, 0);
        zeros.addEdge(1, 2, 2);
        verifyDistances(zeros);
        verifyDistances(new Graph(1, true));
        Graph large = new Graph(3, false);
        large.addEdge(0, 1, Integer.MAX_VALUE);
        large.addEdge(1, 2, Integer.MAX_VALUE);
        check(Dijkstra.findPaths(large, 0).getDistance(2) == 4294967294L,
                "Large shortest distance");
        verifyDistances(large);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 1, 0);
        verifyDistances(graph);
    }

    private static void verifyDistances(Graph graph) {
        int n = graph.getVertexCount();
        long inf = Long.MAX_VALUE;
        long[][] expected = new long[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                expected[i][j] = i == j ? 0 : inf;
            }
            for (Edge edge : graph.getNeighbors(i)) {
                expected[i][edge.to] = Math.min(expected[i][edge.to], edge.weight);
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (expected[i][k] != inf && expected[k][j] != inf) {
                        expected[i][j] = Math.min(expected[i][j], expected[i][k] + expected[k][j]);
                    }
                }
            }
        }
        for (int source = 0; source < n; source++) {
            ShortestPathResult result = Dijkstra.findPaths(graph, source);
            for (int vertex = 0; vertex < n; vertex++) {
                check(result.getDistance(vertex) == expected[source][vertex], "Floyd-Warshall distance");
                List<Integer> path = result.getPath(vertex);
                if (expected[source][vertex] == inf) {
                    check(path.isEmpty(), "Unreachable path empty");
                    continue;
                }
                check(path.get(0) == source && path.get(path.size() - 1) == vertex, "Path endpoints");
                boolean[] seen = new boolean[n];
                long sum = 0;
                for (int i = 0; i < path.size(); i++) {
                    int current = path.get(i);
                    check(!seen[current], "Path has no repeated vertices");
                    seen[current] = true;
                    if (i + 1 < path.size()) {
                        int next = path.get(i + 1);
                        long best = inf;
                        for (Edge edge : graph.getNeighbors(current)) {
                            if (edge.to == next) {
                                best = Math.min(best, edge.weight);
                            }
                        }
                        check(best != inf, "Path edge exists");
                        sum += best;
                    }
                }
                check(sum == result.getDistance(vertex), "Path weight matches distance");
            }
        }
    }

    private static void testRandomGraphs() {
        Random random = new Random(42);
        for (int repeat = 0; repeat < 20; repeat++) {
            Graph graph = new Graph(7, true);
            Graph dag = new Graph(7, true);
            for (int from = 0; from < 7; from++) {
                for (int to = 0; to < 7; to++) {
                    if (random.nextInt(4) == 0) {
                        graph.addEdge(from, to, random.nextInt(10));
                        if (from < to) {
                            dag.addEdge(from, to, 1);
                        }
                    }
                }
            }
            verifySCC(graph);
            verifyDistances(graph);
            verifyOrder(dag);
        }
    }

    private static void testDeepChain() {
        Graph graph = new Graph(20000, true);
        for (int i = 1; i < 20000; i++) {
            graph.addEdge(i - 1, i, 1);
        }
        check(Kosaraju.findComponents(graph).size() == 20000, "Iterative DFS handles long chain");
        check(TopologicalSort.sort(graph).size() == 20000, "Long DAG order");
    }

    private static void testInvalidInput() {
        Graph undirected = new Graph(2, false);
        expectInvalid(() -> TopologicalSort.sort(undirected));
        expectInvalid(() -> Kosaraju.findComponents(undirected));
        expectInvalid(() -> Dijkstra.findPaths(undirected, 2));
        Graph negative = new Graph(3, true);
        negative.addEdge(1, 2, -1);
        expectInvalid(() -> Dijkstra.findPaths(negative, 0));
    }

    private static void expectInvalid(Runnable action) {
        boolean failed = false;
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            failed = true;
        }
        check(failed, "Invalid input rejected");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
