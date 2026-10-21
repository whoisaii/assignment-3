import app.Generators;
import graph.*;
import dsu.DSU;
import mst.MinimumSpanningTree;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.util.*;

/** No test framework dependency. Checks throw even without JVM -ea. */
public final class TestSuite {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++; if (!value) throw new AssertionError(message);
    }
    private static void rejects(Runnable action) {
        boolean thrown = false;
        try { action.run(); } catch (IllegalArgumentException expected) { thrown = true; }
        check(thrown, "Expected IllegalArgumentException");
    }
    private static void mst(Graph g, long weight, int components) {
        for (var result : List.of(MinimumSpanningTree.kruskal(g,new Metrics()), MinimumSpanningTree.prim(g,new Metrics()))) {
            check(result.weight() == weight, "MST weight");
            check(result.components() == components, "Component count");
            check(result.connected() == (components == 1), "Connectivity flag");
            check(result.edges().size() == g.size()-components, "Forest edge count");
            DSU dsu = new DSU(g.size(),new Metrics()); long sum = 0;
            for (var e : result.edges()) { check(dsu.union(e.from(),e.to()),"Forest has cycle"); sum += e.weight(); }
            check(sum == weight,"Selected edge weights");
        }
    }
    private static void validateTopology(Graph g) {
        var result = Connectivity.topological(g,new Metrics()); check(result.acyclic(),"Expected DAG");
        int[] pos = new int[g.size()]; Arrays.fill(pos,-1);
        check(result.order().size() == g.size(),"Ordering size");
        for (int i=0;i<result.order().size();i++) {
            int v=result.order().get(i);check(pos[v] == -1,"Duplicate topological vertex");pos[v]=i;
        }
        for (var e:g.edges()) check(pos[e.from()] < pos[e.to()],"Invalid topological edge");
    }
    private static long[][] floyd(Graph g) {
        long[][] d = new long[g.size()][g.size()];
        for (int i=0;i<g.size();i++) {Arrays.fill(d[i],Long.MAX_VALUE);d[i][i]=0;}
        for (int v=0;v<g.size();v++) for(var e:g.neighbors(v)) d[v][e.to()]=Math.min(d[v][e.to()],e.weight());
        for(int k=0;k<g.size();k++) for(int i=0;i<g.size();i++) for(int j=0;j<g.size();j++)
            if(d[i][k]!=Long.MAX_VALUE && d[k][j]!=Long.MAX_VALUE) d[i][j]=Math.min(d[i][j],d[i][k]+d[k][j]);
        return d;
    }
    private static void validateRoutes(Graph g) {
        long[][] expected=floyd(g);
        for(int source=0;source<g.size();source++) {
            var result=Dijkstra.run(g,source,new Metrics());
            for(int v=0;v<g.size();v++) {
                check(result.distanceTo(v)==expected[source][v],"Dijkstra vs Floyd-Warshall");
                var path=result.pathTo(v);
                if(expected[source][v]==Long.MAX_VALUE) {check(path.isEmpty(),"Unreachable path");continue;}
                check(path.get(0)==source && path.get(path.size()-1)==v,"Path endpoints");
                check(new HashSet<>(path).size()==path.size(),"Path cycle");long sum=0;
                for(int i=1;i<path.size();i++) {
                    int a=path.get(i-1),b=path.get(i);int weight=Integer.MAX_VALUE;boolean found=false;
                    for(var e:g.neighbors(a)) if(e.to()==b) {found=true;weight=Math.min(weight,e.weight());}
                    check(found,"Missing path edge");sum+=weight;
                }
                check(sum==result.distanceTo(v),"Path cost");
            }
        }
    }
    private static void validateScc(Graph g) {
        var components=Connectivity.scc(g,new Metrics()); int[] owner=new int[g.size()];Arrays.fill(owner,-1);
        for(int c=0;c<components.size();c++) for(int v:components.get(c)) {
            check(owner[v]==-1,"Vertex in multiple SCCs");owner[v]=c;
        }
        long[][] reach=floyd(g);
        for(int a=0;a<g.size();a++) {
            check(owner[a]>=0,"Missing SCC vertex");
            for(int b=0;b<g.size();b++) check((owner[a]==owner[b]) ==
                    (reach[a][b]!=Long.MAX_VALUE && reach[b][a]!=Long.MAX_VALUE),"SCC mutual reachability");
        }
    }
    private static long bruteMst(Graph g) {
        long best=Long.MAX_VALUE;int e=g.edges().size();
        for(int mask=0;mask<(1<<e);mask++) {
            if(Integer.bitCount(mask)!=g.size()-1) continue;
            DSU d=new DSU(g.size(),new Metrics());long sum=0;boolean tree=true;
            for(int i=0;i<e;i++) if((mask & (1<<i))!=0) {
                var edge=g.edges().get(i);if(!d.union(edge.from(),edge.to())) {tree=false;break;}sum+=edge.weight();
            }
            if(tree) best=Math.min(best,sum);
        }
        return best;
    }
    public static void main(String[] args) {
        Metrics m=new Metrics();DSU d=new DSU(8,m);
        check(d.union(0,1),"Initial union");check(!d.union(1,0),"Duplicate union");
        d.union(2,3);d.union(0,2);d.union(4,5);d.union(6,7);d.union(4,6);d.union(0,4);
        int root=d.find(7);long before=m.parentHops;check(d.find(7)==root,"Compressed root");
        check(m.parentHops-before<=2,"Compressed path length");
        mst(new Graph(1,false),0,1);
        mst(new Graph(4,false),0,4);
        mst(new Graph(5,false).add(0,1,2).add(1,2,3).add(3,4,1),6,2);
        mst(new Graph(4,false).add(0,1,1).add(1,2,1).add(2,3,1).add(3,0,1).add(0,2,1),3,1);
        mst(new Graph(3,false).add(0,1,-2).add(1,2,0).add(0,2,4).add(0,0,-10).add(0,1,5),-2,1);
        mst(new Graph(3,false).add(0,1,Integer.MAX_VALUE).add(1,2,Integer.MAX_VALUE),4294967294L,1);
        validateTopology(new Graph(1,true));validateTopology(new Graph(5,true));
        Graph cycle=new Graph(4,true).add(0,1,1).add(1,0,1).add(2,3,1);
        check(!Connectivity.topological(cycle,new Metrics()).acyclic(),"Cycle detection");
        check(Connectivity.topological(cycle,new Metrics()).order().isEmpty(),"No partial order advertised");
        check(!Connectivity.topological(new Graph(1,true).add(0,0,0),new Metrics()).acyclic(),"Self-loop cycle");
        validateScc(cycle);validateScc(new Graph(1,true));validateScc(new Graph(5,true));
        validateRoutes(new Graph(1,true));
        validateRoutes(new Graph(5,true).add(0,1,0).add(1,0,0).add(1,2,2).add(0,2,9).add(0,2,3));
        validateRoutes(new Graph(3,true).add(0,1,Integer.MAX_VALUE).add(1,2,Integer.MAX_VALUE));
        validateRoutes(new Graph(4,false).add(0,1,2).add(1,2,3));
        rejects(()->Dijkstra.run(new Graph(3,true).add(1,2,-1),0,new Metrics()));
        rejects(()->Dijkstra.run(new Graph(1,true),1,new Metrics()));
        rejects(()->MinimumSpanningTree.prim(new Graph(1,true),new Metrics()));
        rejects(()->MinimumSpanningTree.kruskal(new Graph(1,true),new Metrics()));
        rejects(()->Connectivity.scc(new Graph(1,false),new Metrics()));
        rejects(()->Connectivity.topological(new Graph(1,false),new Metrics()));
        rejects(()->new Graph(0,false));rejects(()->new Graph(2,true).add(2,0,1));
        for(int seed=0;seed<30;seed++) {
            Graph small=Generators.generate(6,9,seed,"undirected");mst(small,bruteMst(small),1);
            validateTopology(Generators.generate(10,30,seed,"dag"));
            Graph routes=Generators.generate(10,30,seed,"routes");validateRoutes(routes);validateScc(routes);
            Graph cyclic=Generators.generate(10,30,seed,"cyclic");
            check(!Connectivity.topological(cyclic,new Metrics()).acyclic(),"Generated cycle");
            Graph same=Generators.generate(6,9,seed,"undirected");check(small.edges().equals(same.edges()),"Fixed-seed reproducibility");
        }
        Graph chain=new Graph(20000,true);for(int i=0;i<19999;i++) chain.add(i,i+1,1);
        check(Connectivity.scc(chain,new Metrics()).size()==20000,"Iterative SCC deep chain");
        validateTopology(chain);
        System.out.println("PASS: " + checks + " correctness checks; 30 random seeds and a 20,000-vertex chain.");
    }
}
