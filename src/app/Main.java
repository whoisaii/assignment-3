package app;

import graph.*;
import mst.MinimumSpanningTree;
import connectivity.Connectivity;
import shortestpath.Dijkstra;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        Graph city = new Graph(6, false).add(0,1,4).add(0,2,2).add(1,2,1)
                .add(1,3,5).add(2,3,8).add(3,4,2).add(4,5,3).add(2,5,10);
        var k = MinimumSpanningTree.kruskal(city, new Metrics());
        var p = MinimumSpanningTree.prim(city, new Metrics());
        System.out.println("City districts: 0..5; road weights: construction costs");
        System.out.println("Kruskal: " + k); System.out.println("Prim: " + p);
        Graph services = new Graph(6, true).add(0,1,1).add(0,2,1).add(1,3,1).add(2,3,1).add(3,4,1);
        System.out.println("Service dependency DAG: " + Connectivity.topological(services, new Metrics()));
        Graph roads = new Graph(6, true).add(0,1,4).add(1,2,1).add(2,0,2).add(2,3,5).add(3,4,2).add(4,3,2);
        System.out.println("One-way road SCCs: " + Connectivity.scc(roads, new Metrics()));
        System.out.println("Cyclic topological result: " + Connectivity.topological(roads, new Metrics()));
        var routes = Dijkstra.run(roads, 0, new Metrics());
        for (int v = 0; v < roads.size(); v++) System.out.println("0 -> " + v + ": "
                + (routes.distanceTo(v) == Dijkstra.INF ? "unreachable" : routes.distanceTo(v))
                + "; path=" + routes.pathTo(v));
    }
}
