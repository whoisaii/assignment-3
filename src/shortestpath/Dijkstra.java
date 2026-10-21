package shortestpath;

import graph.*;
import java.util.*;

public final class Dijkstra {
    private Dijkstra() {}
    public static final long INF = Long.MAX_VALUE;
    private record Entry(int vertex, long distance) {}
    public static final class Result {
        private final long[] distance;
        private final int[] previous;
        private Result(long[] distance, int[] previous) { this.distance = distance; this.previous = previous; }
        public long distanceTo(int v) { return distance[v]; }
        public List<Integer> pathTo(int v) {
            if (distance[v] == INF) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int at = v; at != -1; at = previous[at]) path.add(at);
            Collections.reverse(path); return List.copyOf(path);
        }
    }
    public static Result run(Graph g, int source, Metrics m) {
        g.checkVertex(source);
        for (Graph.Edge e : g.edges()) {
            m.edgeExaminations++;
            if (e.weight() < 0) throw new IllegalArgumentException("Dijkstra requires non-negative weights");
        }
        long[] distance = new long[g.size()]; int[] previous = new int[g.size()];
        Arrays.fill(distance, INF); Arrays.fill(previous, -1); distance[source] = 0;
        PriorityQueue<Entry> q = new PriorityQueue<>((a, b) -> {
            m.comparisons++;
            int c = Long.compare(a.distance(), b.distance());
            return c != 0 ? c : Integer.compare(a.vertex(), b.vertex());
        });
        q.offer(new Entry(source, 0)); m.queueOffers++;
        while (!q.isEmpty()) {
            Entry current = q.poll(); m.queuePolls++;
            if (current.distance() != distance[current.vertex()]) continue;
            for (Graph.Edge e : g.neighbors(current.vertex())) {
                m.edgeExaminations++;
                long candidate = current.distance() + (long)e.weight();
                if (candidate < distance[e.to()]) {
                    distance[e.to()] = candidate; previous[e.to()] = current.vertex();
                    q.offer(new Entry(e.to(), candidate)); m.queueOffers++;
                }
            }
        }
        return new Result(distance, previous);
    }
}
