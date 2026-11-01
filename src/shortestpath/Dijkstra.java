package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra {
    private static class State {
        final int vertex;
        final long distance;

        State(int vertex, long distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    public static ShortestPathResult findPaths(Graph graph, int source) {
        int n = graph.getVertexCount();
        if (source < 0 || source >= n) {
            throw new IllegalArgumentException("Invalid source: " + source);
        }
        for (Edge edge : graph.getEdges()) {
            if (edge.weight < 0) {
                throw new IllegalArgumentException("Dijkstra requires non-negative weights");
            }
        }
        long[] distances = new long[n];
        int[] previous = new int[n];
        Arrays.fill(distances, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        distances[source] = 0;
        PriorityQueue<State> queue = new PriorityQueue<>(
                Comparator.comparingLong((State state) -> state.distance));
        queue.offer(new State(source, 0));
        while (!queue.isEmpty()) {
            State current = queue.poll();
            if (current.distance != distances[current.vertex]) {
                continue;
            }
            for (Edge edge : graph.getNeighbors(current.vertex)) {
                long candidate = current.distance + edge.weight;
                if (candidate < distances[edge.to]) {
                    distances[edge.to] = candidate;
                    previous[edge.to] = current.vertex;
                    queue.offer(new State(edge.to, candidate));
                }
            }
        }
        return new ShortestPathResult(source, distances, previous);
    }
}
