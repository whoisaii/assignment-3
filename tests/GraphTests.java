package tests;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;

public class GraphTests {
    private static int checks = 0;

    public static void main(String[] args) {
        testGraph();
        testDSU();
        testKnownGraph();
        testDisconnected();
        testEqualWeights();
        testSingleVertex();
        testParallelEdgesAndLoops();
        testLargeWeight();
        testInvalidInput();
        System.out.println("All " + checks + " checks passed.");
    }

    private static void testGraph() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, 7);
        check(graph.getEdges().size() == 1, "One logical undirected edge");
        check(graph.getNeighbors(0).get(0).to == 1, "Forward adjacency");
        check(graph.getNeighbors(1).get(0).to == 0, "Reverse adjacency");
        check(graph.getNeighbors(2).isEmpty(), "Isolated vertex");
        Graph directed = new Graph(2, true);
        directed.addEdge(0, 1, 7);
        check(directed.getNeighbors(1).isEmpty(), "No reverse directed edge");
        expectFailure(() -> graph.getEdges().clear(), "Protect edge list");
    }

    private static void testDSU() {
        DSU dsu = new DSU(5);
        check(dsu.getComponents() == 5, "Initial components");
        check(dsu.union(0, 1), "First union");
        check(dsu.union(2, 3), "Second union");
        check(dsu.union(0, 2), "Merge groups");
        check(dsu.find(3) == dsu.find(1), "Same representative");
        check(!dsu.union(1, 3), "Repeated union");
        check(dsu.getComponents() == 2, "Component count after unions");
        check(dsu.find(4) != dsu.find(0), "Separate group");
    }

    private static void testKnownGraph() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 2, 1);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, 5);
        verifyBoth(graph, 6, 1);
        check(graph.getEdges().size() == 5, "Algorithms preserve input");
    }

    private static void testDisconnected() {
        Graph graph = new Graph(5, false);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 5);
        verifyBoth(graph, 7, 3);
        verifyBoth(new Graph(4, false), 0, 4);
    }

    private static void testEqualWeights() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);
        graph.addEdge(0, 2, 1);
        verifyBoth(graph, 3, 1);
    }

    private static void testSingleVertex() {
        verifyBoth(new Graph(1, false), 0, 1);
    }

    private static void testParallelEdgesAndLoops() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, 9);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, -3);
        graph.addEdge(0, 2, 10);
        verifyBoth(graph, -1, 1);
    }

    private static void testLargeWeight() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, Integer.MAX_VALUE);
        graph.addEdge(1, 2, Integer.MAX_VALUE);
        verifyBoth(graph, 4294967294L, 1);
    }

    private static void testInvalidInput() {
        expectFailure(() -> new Graph(0, false), "Empty graph");
        Graph graph = new Graph(2, false);
        expectFailure(() -> graph.addEdge(0, 2, 1), "Invalid endpoint");
        expectFailure(() -> graph.getNeighbors(-1), "Invalid neighbor lookup");
        expectFailure(() -> new DSU(0), "Empty DSU");
        expectFailure(() -> new DSU(2).find(2), "Invalid DSU vertex");
        Graph directed = new Graph(2, true);
        expectFailure(() -> Kruskal.findMST(directed), "Directed Kruskal input");
        expectFailure(() -> Prim.findMST(directed), "Directed Prim input");
    }

    private static void verifyBoth(Graph graph, long weight, int components) {
        MSTResult kruskal = Kruskal.findMST(graph);
        MSTResult prim = Prim.findMST(graph);
        verifyForest(graph, kruskal, weight, components);
        verifyForest(graph, prim, weight, components);
        check(kruskal.totalWeight == prim.totalWeight, "MST weights agree");
    }

    private static void verifyForest(Graph graph, MSTResult result,
                                     long weight, int components) {
        check(result.totalWeight == weight, "Expected total weight");
        check(result.components == components, "Expected components");
        check(result.isConnected() == (components == 1), "Connectivity flag");
        check(result.edges.size() == graph.getVertexCount() - components,
                "Forest edge count");
        DSU forest = new DSU(graph.getVertexCount());
        long sum = 0;
        for (Edge edge : result.edges) {
            check(graph.getNeighbors(edge.from).stream().anyMatch(original ->
                    original.to == edge.to && original.weight == edge.weight),
                    "Selected edge exists in graph");
            check(forest.union(edge.from, edge.to), "No cycle in forest");
            sum += edge.weight;
        }
        check(sum == result.totalWeight, "Sum of selected weights");
        check(forest.getComponents() == components, "Forest component count");
        for (Edge edge : graph.getEdges()) {
            check(forest.find(edge.from) == forest.find(edge.to),
                    "Forest spans each input component");
        }
    }

    private static void expectFailure(Runnable action, String message) {
        boolean failed = false;
        try {
            action.run();
        } catch (IllegalArgumentException | UnsupportedOperationException exception) {
            failed = true;
        }
        check(failed, message);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}