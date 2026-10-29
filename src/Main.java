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
