package tests;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import experiments.GraphGenerator;
import graph.Graph;
import graph.Metrics;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;

public class ExperimentTests {
    public static void main(String[] args) {
        for (int[] size : new int[][] {{10, 30}, {100, 500}, {1000, 5000}}) {
            int v = size[0];
            int e = size[1];
            long seed = 42L + v;
            Graph graph = GraphGenerator.connectedUndirected(v, e, seed);
            Graph again = GraphGenerator.connectedUndirected(v, e, seed);
            check(graph.getEdges().size() == e, "Exact edge count");
            for (int i = 0; i < e; i++) {
                check(graph.getEdges().get(i).toString().equals(again.getEdges().get(i).toString()), "Seed reproducibility");
            }
            check(Kruskal.findMST(graph).isConnected(), "Generator connected");
            check(Kruskal.findMST(graph).totalWeight == Prim.findMST(graph).totalWeight, "MST agreement");
            Metrics prim = new Metrics();
            Prim.findMST(graph, prim);
            check(prim.edgeChecks == 2L * e, "Prim adjacency scans");
            Graph cyclic = GraphGenerator.cyclicDirected(v, e, seed);
            Metrics scc = new Metrics();
            Kosaraju.findComponents(cyclic, scc);
            check(scc.edgeChecks == 3L * e, "Kosaraju scans");
            Metrics topology = new Metrics();
            check(TopologicalSort.sort(GraphGenerator.dag(v, e, seed), topology).size() == v, "Generated DAG");
            check(topology.edgeChecks == 2L * e, "Kahn DAG scans");
            boolean detected = false;
            try {
                TopologicalSort.sort(cyclic);
            } catch (IllegalStateException exception) {
                detected = true;
            }
            check(detected, "Generated cycle");
            Graph routes = GraphGenerator.directed(v, e, seed);
            Metrics first = new Metrics();
            Metrics second = new Metrics();
            Dijkstra.findPaths(routes, 0, first);
            Dijkstra.findPaths(routes, 0, second);
            check(first.toCsv().equals(second.toCsv()), "Repeatable counters");
        }
        System.out.println("Experiment checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
