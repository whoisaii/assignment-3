package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestPathResult {
    private final int source;
    private final long[] distances;
    private final int[] previous;

    public ShortestPathResult(int source, long[] distances, int[] previous) {
        this.source = source;
        this.distances = distances.clone();
        this.previous = previous.clone();
    }

    public long getDistance(int vertex) {
        checkVertex(vertex);
        return distances[vertex];
    }

    public List<Integer> getPath(int vertex) {
        checkVertex(vertex);
        if (distances[vertex] == Long.MAX_VALUE) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        int current = vertex;
        while (current != source) {
            path.add(current);
            current = previous[current];
        }
        path.add(source);
        Collections.reverse(path);
        return List.copyOf(path);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
    }
}
