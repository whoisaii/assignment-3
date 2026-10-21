package connectivity;

import graph.*;
import java.util.*;

public final class Connectivity {
    private Connectivity() {}
    public record Topology(boolean acyclic, List<Integer> order) {}
    private static void requireDirected(Graph g) {
        if (!g.directed()) throw new IllegalArgumentException("Directed graph required");
    }
    public static Topology topological(Graph g, Metrics m) {
        requireDirected(g);
        int[] indegree = new int[g.size()];
        for (Graph.Edge e : g.edges()) { indegree[e.to()]++; m.edgeExaminations++; }
        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < g.size(); v++) if (indegree[v] == 0) { q.add(v); m.queueOffers++; }
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int v = q.remove(); m.queuePolls++; order.add(v);
            for (Graph.Edge e : g.neighbors(v)) {
                m.edgeExaminations++;
                if (--indegree[e.to()] == 0) { q.add(e.to()); m.queueOffers++; }
            }
        }
        boolean dag = order.size() == g.size();
        return new Topology(dag, dag ? List.copyOf(order) : List.of());
    }
    /** Kosaraju with explicit stacks, including a per-vertex DFS cursor for finishing order. */
    public static List<List<Integer>> scc(Graph g, Metrics m) {
        requireDirected(g);
        int n = g.size(); boolean[] seen = new boolean[n]; int[] next = new int[n];
        List<Integer> finish = new ArrayList<>(); ArrayDeque<Integer> stack = new ArrayDeque<>();
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            seen[root] = true; stack.push(root);
            while (!stack.isEmpty()) {
                int v = stack.peek();
                if (next[v] < g.neighbors(v).size()) {
                    int w = g.neighbors(v).get(next[v]++).to(); m.edgeExaminations++;
                    if (!seen[w]) { seen[w] = true; stack.push(w); }
                } else { finish.add(stack.pop()); }
            }
        }
        List<List<Integer>> reverse = new ArrayList<>();
        for (int v = 0; v < n; v++) reverse.add(new ArrayList<>());
        for (Graph.Edge e : g.edges()) { reverse.get(e.to()).add(e.from()); m.edgeExaminations++; }
        Arrays.fill(seen, false); List<List<Integer>> components = new ArrayList<>();
        for (int i = finish.size() - 1; i >= 0; i--) {
            int root = finish.get(i); if (seen[root]) continue;
            List<Integer> component = new ArrayList<>(); seen[root] = true; stack.push(root);
            while (!stack.isEmpty()) {
                int v = stack.pop(); component.add(v);
                for (int w : reverse.get(v)) {
                    m.edgeExaminations++;
                    if (!seen[w]) { seen[w] = true; stack.push(w); }
                }
            }
            components.add(List.copyOf(component));
        }
        return List.copyOf(components);
    }
}
