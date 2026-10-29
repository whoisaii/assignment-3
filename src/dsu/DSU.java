
package dsu;

public class DSU {
    private final int[] parent;
    private final int[] size;
    private int components;

    public DSU(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("DSU size must be positive");
        }
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int vertex) {
        if (vertex < 0 || vertex >= parent.length) {
            throw new IllegalArgumentException("Invalid vertex: " + vertex);
        }
        int root = vertex;
        while (root != parent[root]) {
            root = parent[root];
        }
        // Point every vertex on this path directly to the root.
        while (vertex != root) {
            int next = parent[vertex];
            parent[vertex] = root;
            vertex = next;
        }
        return root;
    }

    public boolean union(int first, int second) {
        int rootFirst = find(first);
        int rootSecond = find(second);
        if (rootFirst == rootSecond) {
            return false;
        }
        if (size[rootFirst] < size[rootSecond]) {
            int temp = rootFirst;
            rootFirst = rootSecond;
            rootSecond = temp;
        }
        parent[rootSecond] = rootFirst;
        size[rootFirst] += size[rootSecond];
        components--;
        return true;
    }

    public int getComponents() {
        return components;
    }
}
