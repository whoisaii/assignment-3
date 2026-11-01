import connectivity.Kosaraju;
import connectivity.TopologicalSort;
import shortestpath.Dijkstra;
import shortestpath.ShortestPathResult;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;

public class Main {
    public static void main(String[] args) {
        Graph city = new Graph(4, false);
        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 3);
        city.addEdge(1, 2, 1);
        city.addEdge(1, 3, 2);
        city.addEdge(2, 3, 5);

        printResult("Kruskal", Kruskal.findMST(city));
        printResult("Prim", Prim.findMST(city));

        Graph disconnected = new Graph(4, false);
        disconnected.addEdge(0, 1, 2);
        disconnected.addEdge(2, 3, 5);
        printResult("Disconnected graph (Kruskal)", Kruskal.findMST(disconnected));
        printResult("Disconnected graph (Prim)", Prim.findMST(disconnected));

        Graph dependencies = new Graph(4, true);
        dependencies.addEdge(0, 2, 1);
        dependencies.addEdge(1, 2, 1);
        dependencies.addEdge(2, 3, 1);
        System.out.println("Topological order: " + TopologicalSort.sort(dependencies));
        dependencies.addEdge(3, 0, 1);
        try {
            TopologicalSort.sort(dependencies);
        } catch (IllegalStateException exception) {
            System.out.println("Topological order unavailable: " + exception.getMessage());
        }

        Graph roads = new Graph(6, true);
        roads.addEdge(0, 1, 1);
        roads.addEdge(1, 2, 1);
        roads.addEdge(2, 0, 1);
        roads.addEdge(2, 3, 1);
        roads.addEdge(3, 4, 1);
        roads.addEdge(4, 3, 1);
        System.out.println("Strongly connected components: " + Kosaraju.findComponents(roads));

        Graph routes = new Graph(4, true);
        routes.addEdge(0, 1, 4);
        routes.addEdge(0, 2, 10);
        routes.addEdge(1, 2, 3);
        ShortestPathResult paths = Dijkstra.findPaths(routes, 0);
        for (int v = 0; v < routes.getVertexCount(); v++) {
            String distance = paths.getDistance(v) == Long.MAX_VALUE
                    ? "unreachable" : Long.toString(paths.getDistance(v));
            System.out.println("0 -> " + v + ": " + distance + ", path " + paths.getPath(v));
        }
    }

    private static void printResult(String name, MSTResult result) {
        System.out.println(name);
        for (Edge edge : result.edges) {
            System.out.println("  " + edge);
        }
        System.out.println("Total weight: " + result.totalWeight);
        System.out.println("Components: " + result.components);
        System.out.println(result.isConnected() ? "Connected: spanning tree"
                : "Disconnected: spanning forest");
        System.out.println();
    }
}
