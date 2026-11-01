package experiments;

import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import graph.Graph;
import graph.Metrics;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Benchmark {
    private static volatile Object lastResult;
    private static final int REPEATS = 5;
    private static final int WARMUPS = 20;

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        Path tables = Path.of("results", "tables");
        Files.createDirectories(tables);
        try (PrintWriter environment = new PrintWriter(Files.newBufferedWriter(tables.resolve("environment.txt")))) {
            environment.println("Java: " + System.getProperty("java.version"));
            environment.println("VM: " + System.getProperty("java.vm.name"));
            environment.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            environment.println("Architecture: " + System.getProperty("os.arch"));
            environment.println("Available processors: " + Runtime.getRuntime().availableProcessors());
            environment.println("Measured at UTC: " + java.time.Instant.now());
            environment.println("Seed: 42 + V; weights: 1..100; warmups: " + WARMUPS + "; repeats: " + REPEATS);
        }
        try (PrintWriter raw = new PrintWriter(Files.newBufferedWriter(tables.resolve("raw.csv")));
             PrintWriter summary = new PrintWriter(Files.newBufferedWriter(tables.resolve("summary.csv")))) {
            String counters = "edge_checks,comparisons,find_calls,union_calls,parent_hops,queue_offers,queue_polls";
            raw.println("algorithm,vertices,edges,seed,run,time_ns," + counters);
            summary.println("algorithm,vertices,edges,seed,average_time_ns,stddev_time_ns," + counters);
            int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
            for (int[] size : sizes) {
                int v = size[0];
                int e = size[1];
                long seed = 42L + v;
                Graph undirected = GraphGenerator.connectedUndirected(v, e, seed);
                Graph dag = GraphGenerator.dag(v, e, seed);
                Graph cyclic = GraphGenerator.cyclicDirected(v, e, seed);
                Graph routing = GraphGenerator.directed(v, e, seed);
                if (Kruskal.findMST(undirected).totalWeight != Prim.findMST(undirected).totalWeight
                        || !Kruskal.findMST(undirected).isConnected()) {
                    throw new IllegalStateException("MST precheck failed");
                }
                TopologicalSort.sort(dag);
                if (!hasCycle(cyclic, new Metrics())) {
                    throw new IllegalStateException("Cycle precheck failed");
                }
                measure("Kruskal", undirected, seed, raw, summary);
                measure("Prim", undirected, seed, raw, summary);
                measure("SCC", cyclic, seed, raw, summary);
                measure("Topological-DAG", dag, seed, raw, summary);
                measure("Topological-cycle", cyclic, seed, raw, summary);
                measure("Dijkstra", routing, seed, raw, summary);
            }
        }
        System.out.println("Saved 90 measurements and 18 summaries in results/tables");
    }

    private static boolean hasCycle(Graph graph, Metrics metrics) {
        try {
            TopologicalSort.sort(graph, metrics);
            return false;
        } catch (IllegalStateException exception) {
            return true;
        }
    }

    private static Object run(String algorithm, Graph graph, Metrics metrics) {
        switch (algorithm) {
            case "Kruskal": return Kruskal.findMST(graph, metrics);
            case "Prim": return Prim.findMST(graph, metrics);
            case "SCC": return Kosaraju.findComponents(graph, metrics);
            case "Topological-DAG": return TopologicalSort.sort(graph, metrics);
            case "Topological-cycle": return hasCycle(graph, metrics);
            case "Dijkstra": return Dijkstra.findPaths(graph, 0, metrics);
            default: throw new IllegalArgumentException(algorithm);
        }
    }

    private static void measure(String algorithm, Graph graph, long seed,
                                PrintWriter raw, PrintWriter summary) {
        for (int i = 0; i < WARMUPS; i++) {
            lastResult = run(algorithm, graph, new Metrics());
        }
        long[] times = new long[REPEATS];
        String counts = null;
        for (int i = 0; i < REPEATS; i++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            Object result = run(algorithm, graph, metrics);
            times[i] = System.nanoTime() - start;
            lastResult = result;
            if (counts != null && !counts.equals(metrics.toCsv())) {
                throw new IllegalStateException("Operation counts changed between repeats");
            }
            counts = metrics.toCsv();
            raw.printf("%s,%d,%d,%d,%d,%d,%s%n", algorithm, graph.getVertexCount(),
                    graph.getEdges().size(), seed, i + 1, times[i], counts);
        }
        double mean = 0;
        for (long time : times) {
            mean += (double) time / REPEATS;
        }
        double variance = 0;
        for (long time : times) {
            variance += (time - mean) * (time - mean) / (REPEATS - 1);
        }
        summary.printf("%s,%d,%d,%d,%.3f,%.3f,%s%n", algorithm, graph.getVertexCount(),
                graph.getEdges().size(), seed, mean, Math.sqrt(variance), counts);
    }
}
