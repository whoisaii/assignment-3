package app;

import graph.*;
import mst.MinimumSpanningTree;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class Benchmark {
    private Benchmark() {}
    private static volatile Object blackhole;
    private static Object execute(String algorithm, Graph g, Metrics m) {
        return switch (algorithm) {
            case "Kruskal" -> MinimumSpanningTree.kruskal(g, m);
            case "Prim" -> MinimumSpanningTree.prim(g, m);
            case "SCC" -> Connectivity.scc(g, m);
            case "Topological-DAG", "Topological-cycle" -> Connectivity.topological(g, m);
            case "Dijkstra" -> Dijkstra.run(g, 0, m);
            default -> throw new IllegalArgumentException(algorithm);
        };
    }
    public static void main(String[] args) throws IOException {
        Path output = Path.of("results/tables"); Files.createDirectories(output);
        Files.writeString(output.resolve("environment.txt"), "Recorded at: " + java.time.Instant.now()
                + "\nJava: " + System.getProperty("java.version") + "\nVM: " + System.getProperty("java.vm.name")
                + "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + "\nArchitecture: " + System.getProperty("os.arch")
                + "\nProcessors available: " + Runtime.getRuntime().availableProcessors()
                + "\nWarmups: 20 per graph/algorithm; measured repetitions: 5\n");
        try (PrintWriter raw = new PrintWriter(Files.newBufferedWriter(output.resolve("raw.csv")));
             PrintWriter summary = new PrintWriter(Files.newBufferedWriter(output.resolve("summary.csv")))) {
            raw.println("algorithm,kind,vertices,edges,seed,run,nanoseconds,edge_examinations,comparisons,find_calls,parent_hops,union_calls,queue_offers,queue_polls");
            summary.println("algorithm,kind,vertices,edges,seed,mean_ns,min_ns,max_ns,stddev_ns,edge_examinations,comparisons,find_calls,parent_hops,union_calls,queue_offers,queue_polls");
            int[][] sizes = {{10,30},{100,500},{1000,5000}};
            String[] algorithms = {"Kruskal","Prim","SCC","Topological-DAG","Topological-cycle","Dijkstra"};
            String[] kinds = {"undirected","undirected","cyclic","dag","cyclic","routes"};
            for (int[] size : sizes) {
                Map<String,Graph> graphs = new HashMap<>(); long seed = 202603L + size[0];
                for (String kind : kinds) graphs.computeIfAbsent(kind, k -> Generators.generate(size[0], size[1], seed, k));
                var kruskal = MinimumSpanningTree.kruskal(graphs.get("undirected"), new Metrics());
                var prim = MinimumSpanningTree.prim(graphs.get("undirected"), new Metrics());
                if (!kruskal.connected() || kruskal.weight() != prim.weight()) throw new AssertionError("MST mismatch");
                if (!Connectivity.topological(graphs.get("dag"), new Metrics()).acyclic()
                        || Connectivity.topological(graphs.get("cyclic"), new Metrics()).acyclic()) throw new AssertionError("Cycle validation failed");
                for (int a = 0; a < algorithms.length; a++) {
                    String algorithm = algorithms[a], kind = kinds[a]; Graph g = graphs.get(kind);
                    for (int warmup = 0; warmup < 20; warmup++) blackhole = execute(algorithm, g, new Metrics());
                    long[] times = new long[5]; String counts = null;
                    for (int run = 0; run < 5; run++) {
                        Metrics m = new Metrics(); long start = System.nanoTime();
                        Object result = execute(algorithm, g, m);
                        times[run] = System.nanoTime() - start; blackhole = result;
                        if (counts != null && !counts.equals(m.csv())) throw new AssertionError("Non-deterministic counters");
                        counts = m.csv();
                        raw.println(algorithm+","+kind+","+size[0]+","+size[1]+","+seed+","+(run+1)+","+times[run]+","+counts);
                    }
                    double mean = Arrays.stream(times).average().orElseThrow();
                    double variance = 0; for (long time : times) variance += (time-mean)*(time-mean);
                    summary.printf(Locale.ROOT, "%s,%s,%d,%d,%d,%.1f,%d,%d,%.1f,%s%n", algorithm,kind,size[0],size[1],seed,
                            mean,Arrays.stream(times).min().orElseThrow(),Arrays.stream(times).max().orElseThrow(),Math.sqrt(variance/4),counts);
                }
            }
        }
        System.out.println("Saved 90 measured runs and 18 summaries to results/tables/.");
    }
}
