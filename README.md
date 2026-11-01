# Assignment 3: Smart City Infrastructure and Route Planning

## Overview

This project uses graphs to represent a city's districts and roads. Each district
is a vertex, numbered from 0 to V-1. An edge connects two districts, and its weight
represents either a construction cost or a travel distance, depending on the task.

The project has three main tasks:

- Find a minimum-cost network using Kruskal and Prim.
- Analyze directed connections using topological sorting and strongly connected components.
- Find shortest routes from a source using Dijkstra.

### How to run

Requirements: JDK 17 or later, a POSIX shell, and Python 3.9 or later for the plots
and report update. No external Java or Python libraries are needed.

Run these commands from the project folder:

```sh
sh run.sh test
sh run.sh demo
sh run.sh benchmark
python3 scripts/plot_results.py
python3 scripts/build_report.py
```

`test` runs the correctness and experiment tests. `demo` prints both MSTs,
a topological order, cycle detection, SCCs, and source-to-vertex routes.
Both MST algorithms return weight 6 on the connected example. On the disconnected
example, both return a forest with weight 7 and two components. The routing
example gives distances 0, 4, 7, and unreachable from source 0; the path to vertex
2 is [0, 1, 2]. The SCC example contains {0,1,2}, {3,4}, and {5}.

The benchmark replaces the CSV files in `results/tables`. After running it, run
both Python scripts to keep the tables, plots, and discussion consistent. The
report script only updates the marked results block below; other text is preserved.

### Project structure

```text
src/
  graph/           Edge, Graph, and Metrics
  dsu/             Disjoint Set Union
  mst/             Kruskal, Prim, and MSTResult
  connectivity/    TopologicalSort and Kosaraju
  shortestpath/    Dijkstra and ShortestPathResult
  experiments/     GraphGenerator and Benchmark
  Main.java        Examples for all algorithms
tests/             Correctness, experiment, and independent MST tests
scripts/           Plot generation and report update
results/
  tables/          Raw measurements, averages, and environment information
  plots/           Time versus vertices and time versus edges
run.sh             Compilation and execution
```

## Implementation

### Graph and DSU

`Graph` stores adjacency lists and a separate list of logical edges. Directed
edges are added in one direction. Undirected edges appear once in the edge list
and in both directions in the adjacency lists. This lets Kruskal use the edge
list while Prim and the traversal algorithms use neighbors.

The graph accepts isolated vertices, parallel edges, repeated weights, and
self-loops. Invalid vertex numbers and graphs with fewer than one vertex are
rejected. Edge weights use `int`; total weights and distances use `long` to avoid
overflow when several large edge weights are added.

DSU uses parent and size arrays. `find` locates the root and compresses the path.
`union` attaches the smaller component to the larger one. If the vertices already
have the same root, it returns false. The component count decreases after each
successful union.

### Minimum spanning tree

Kruskal sorts edges by weight and accepts an edge when DSU joins two different
components. This avoids cycles. It stops once all vertices are connected, or
when there are no edges left.

Prim uses Java's `PriorityQueue` to choose a cheap edge leading to an unvisited
vertex. Entries leading to visited vertices are skipped. This is lazy Prim, so
the heap can contain O(E) entries. The outer loop starts again for unvisited
components, which also handles disconnected graphs.

Both algorithms return selected edges, total weight, and the number of
components. A disconnected graph has a minimum spanning forest rather than a
single spanning tree. With equal weights, different edge sets can be correct,
so tests check total weight and forest structure rather than identical edges.

### Directed connectivity

Topological sorting uses Kahn's algorithm. It counts incoming edges, queues
vertices with indegree zero, and reduces the indegrees of their neighbors.
If fewer than V vertices are processed, it throws `IllegalStateException` because
the graph contains a cycle. A partial order is not returned as a complete answer.

Kosaraju first records DFS finishing order. It then reverses the graph's edges
and visits the reversed graph in reverse finishing order. Each new traversal
forms one SCC. Both passes use explicit stacks. In the first pass, a neighbor
cursor preserves the finishing order without recursive calls.

### Shortest paths

Dijkstra starts with distance zero for the source and `Long.MAX_VALUE` for other
vertices. A shorter distance updates the predecessor and adds a heap entry.
Outdated entries are skipped. `getPath` follows predecessors and reverses the
result. An unreachable vertex has an empty path; the source path is `[source]`.

All negative edges are rejected, including edges in unreachable components.
Dijkstra's usual greedy argument needs non-negative weights. For example,
with s->a=2, s->b=5, and b->a=-10, a path through b improves a's distance to -5
after a might already have been settled. Bellman-Ford is more appropriate for
negative edges, with a separate check for reachable negative cycles.

## Complexity Analysis

V is the number of vertices and E the number of logical edges. The graph itself
uses O(V+E) memory. The table gives additional memory, including algorithm results.
The heaps are binary heaps with duplicate entries, not decrease-key heaps.

| Algorithm | Time | Additional memory |
| --- | --- | --- |
| DSU initialization | O(V) | O(V) |
| DSU find / union | O(alpha(V)) amortized per call | O(1) per call |
| Kruskal | O(V + E log(E+1) + E alpha(V)) | O(V+E) |
| Lazy Prim | O(V + E log(E+1)) | O(V+E) |
| Kosaraju | O(V+E) | O(V+E) |
| Topological sorting | O(V+E) | O(V) |
| Lazy Dijkstra | O(V + E + (E+1) log(E+1)) | O(V+E) |
| Reconstruct one path | O(V) worst case | O(V) |

Union by size limits tree depth: when a vertex becomes deeper, the size of its
component at least doubles. A single find is O(log V) in the worst case.
Path compression shortens future searches. Together they give the inverse
Ackermann amortized bound, which is almost constant for practical graph sizes.

More vertices increase array initialization and vertex processing. More edges
increase scans, sorting, and heap operations. For sparse graphs with E proportional
to V, SCC and topological sorting are linear in V, while sorting and heap methods
are usually O(V log V). Dense simple graphs can have E proportional to V squared,
so edge processing becomes more expensive. The log(E+1) notation also covers
edgeless graphs. For simple graphs, log E can be bounded by O(log V).

## Experimental Setup

| Graph | Vertices | Edges | Seed |
| --- | ---: | ---: | ---: |
| Small | 10 | 30 | 52 |
| Medium | 100 | 500 | 142 |
| Large | 1000 | 5000 | 1042 |

The generator uses `java.util.Random` with seed `42 + V` and weights from 1 to 100.
Repeated weights are allowed, but the generated graphs have no self-loops or
parallel edges. The exact requested edge count is produced.

- Undirected graphs start with a spanning tree: each vertex after 0 connects to
  a random earlier vertex. Extra unused pairs are then added. This guarantees
  connectivity, although it does not sample connected graphs uniformly.
- DAGs only contain edges u->v where u<v, so they cannot contain a cycle.
- Cyclic graphs start with 0->1 and 1->0. Other directed edges are then added.
  These graphs are used for SCC and cyclic topological tests.
- Routing graphs use random directed pairs. They can have cycles or unreachable
  vertices. Dijkstra starts from vertex 0.

Kruskal and Prim receive the same undirected graph. Each algorithm and size has
20 warmup runs followed by five measured runs using `System.nanoTime()`.
Graph generation, printing, and CSV writing are outside the measured interval.
Internal allocation, sorting, validation, and SCC transpose creation are included.
The cyclic topological case also includes throwing and catching the exception.
Path reconstruction is tested separately and is not part of the Dijkstra timing.

The mean is the average of five times. Sample standard deviation uses denominator
4. The same graph is reused, but algorithm state and counters are new for every
run. A volatile field keeps the result after timing. The benchmark checks MST
agreement, graph connectivity, DAG/cycle status, and repeatable operation counts.

### What the operation counters mean

- Edge checks: Kruskal candidates, Prim adjacency entries, SCC scans and transpose
  construction, Kahn indegree/outgoing scans, or Dijkstra validation and reachable scans.
- Comparisons: actual comparator calls made by sorting or a priority queue.
- Find and union: DSU calls; finds inside union are included.
- Parent hops: parent-link steps during both root search and compression.
- Offers and polls: queue insertions/removals, including stale heap entries.
  DFS stack operations are not included.

These are selected operations, not a count of every instruction. Different types
of operations should not be added together as if they had equal cost.

## Results

The following tables and plots were generated from the current Java sources
with the commands above. Environment details for this run are included below.
Timings vary by machine and by run; the seed reproduces the graphs and counts.

<!-- RESULTS_START -->

### Measured execution times

| Algorithm | V | E | Mean (ms) | Sample SD (ms) |
| --- | ---: | ---: | ---: | ---: |
| Kruskal | 10 | 30 | 0.015225 | 0.004705 |
| Prim | 10 | 30 | 0.019042 | 0.002496 |
| SCC | 10 | 30 | 0.024566 | 0.002807 |
| Topological-DAG | 10 | 30 | 0.008217 | 0.001398 |
| Topological-cycle | 10 | 30 | 0.003725 | 0.000662 |
| Dijkstra | 10 | 30 | 0.009742 | 0.000853 |
| Kruskal | 100 | 500 | 0.080358 | 0.001437 |
| Prim | 100 | 500 | 0.108108 | 0.006558 |
| SCC | 100 | 500 | 0.154617 | 0.001513 |
| Topological-DAG | 100 | 500 | 0.058433 | 0.000091 |
| Topological-cycle | 100 | 500 | 0.018759 | 0.000154 |
| Dijkstra | 100 | 500 | 0.081717 | 0.002690 |
| Kruskal | 1000 | 5000 | 0.538267 | 0.023626 |
| Prim | 1000 | 5000 | 0.378342 | 0.027727 |
| SCC | 1000 | 5000 | 0.514525 | 0.008686 |
| Topological-DAG | 1000 | 5000 | 0.155750 | 0.002600 |
| Topological-cycle | 1000 | 5000 | 0.032175 | 0.001075 |
| Dijkstra | 1000 | 5000 | 0.640200 | 0.104140 |

### Operation counts per run

| Algorithm | V | Edge checks | Comparisons | Find | Union | Parent hops | Offers | Polls |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Kruskal | 10 | 17 | 105 | 34 | 17 | 40 | 0 | 0 |
| Prim | 10 | 60 | 192 | 0 | 0 | 0 | 30 | 30 |
| SCC | 10 | 90 | 0 | 0 | 0 | 0 | 0 | 0 |
| Topological-DAG | 10 | 60 | 0 | 0 | 0 | 0 | 10 | 10 |
| Topological-cycle | 10 | 30 | 0 | 0 | 0 | 0 | 0 | 0 |
| Dijkstra | 10 | 60 | 33 | 0 | 0 | 0 | 13 | 13 |
| Kruskal | 100 | 253 | 3850 | 506 | 253 | 870 | 0 | 0 |
| Prim | 100 | 1000 | 7750 | 0 | 0 | 0 | 500 | 500 |
| SCC | 100 | 1500 | 0 | 0 | 0 | 0 | 0 | 0 |
| Topological-DAG | 100 | 1000 | 0 | 0 | 0 | 0 | 100 | 100 |
| Topological-cycle | 100 | 500 | 0 | 0 | 0 | 0 | 0 | 0 |
| Dijkstra | 100 | 1000 | 1593 | 0 | 0 | 0 | 151 | 151 |
| Kruskal | 1000 | 4113 | 50331 | 8226 | 4113 | 15364 | 0 | 0 |
| Prim | 1000 | 10000 | 110963 | 0 | 0 | 0 | 5000 | 5000 |
| SCC | 1000 | 15000 | 0 | 0 | 0 | 0 | 0 | 0 |
| Topological-DAG | 1000 | 10000 | 0 | 0 | 0 | 0 | 1000 | 1000 |
| Topological-cycle | 1000 | 5010 | 0 | 0 | 0 | 0 | 2 | 2 |
| Dijkstra | 1000 | 9946 | 23857 | 0 | 0 | 0 | 1425 | 1425 |

### Recorded environment

```text
Java: 17.0.17
VM: OpenJDK 64-Bit Server VM
OS: Mac OS X 26.1
Architecture: aarch64
Available processors: 8
Measured at UTC: 2026-11-01T10:26:53.795185Z
Seed: 42 + V; weights: 1..100; warmups: 20; repeats: 5
```

### Comparison of measured results

- V=10: Kruskal/Prim mean-time ratio is 0.80; Kruskal is faster in this recorded run.
- V=100: Kruskal/Prim mean-time ratio is 0.74; Kruskal is faster in this recorded run.
- V=1000: Kruskal/Prim mean-time ratio is 1.42; Prim is faster in this recorded run.

SCC: large/small mean-time ratio is 20.94. This is an observation for these graphs and this JVM run, not an asymptotic bound.

Dijkstra: large/small mean-time ratio is 65.72. This is an observation for these graphs and this JVM run, not an asymptotic bound.


<!-- RESULTS_END -->

### Plots

![Execution time versus number of vertices](results/plots/time-vs-vertices.svg)

![Execution time versus number of edges](results/plots/time-vs-edges.svg)

The bars show sample standard deviation. The two graphs use the same three
(V,E) pairs, so they do not isolate the independent effects of V and E. Lines
connect observations; intermediate sizes were not measured.

All 90 measurements are in [raw.csv](results/tables/raw.csv).
The 18 summaries are in [summary.csv](results/tables/summary.csv).

## Comparison and Discussion

Kruskal spends time sorting all edges but can stop its DSU scan early. Prim
examines all undirected adjacency entries and uses a heap, including stale
entries. A larger comparator count does not automatically mean a longer runtime:
sorting, heap updates, allocation, and JVM optimization have different costs.
The measured winner can change with graph size. These results do not establish
a universal fastest MST algorithm.

Kosaraju examines 3E edges: one scan in the first DFS, one while reversing edges,
and one in the second DFS. The counts are 90, 1500, and 15000. This agrees with
linear edge work; processing vertices adds the O(V) part. Runtime ratios need not
match input-size ratios exactly, especially for these short JVM measurements.

On DAGs, Kahn processes every vertex and examines 2E edges. On cyclic graphs,
it still scans all edges for indegrees, but may process fewer vertices afterward.
Cycle detection can therefore finish faster than a full DAG order on these inputs.
Both cases still have an O(V+E) worst-case bound.

Dijkstra first validates E weights, then scans edges reachable from the source.
Successful improvements create heap entries, and stale entries explain why
queue polls can exceed V. Both reachability and the number of improvements affect
its work, as well as the total number of vertices and edges.

The DSU counters show the optimized implementation inside Kruskal. There is no
unoptimized baseline, so this experiment does not measure a speedup from the DSU
optimizations separately.

Only five repeats and one graph per size/type are used. Algorithm order is fixed,
and there is no control over scheduling, CPU frequency, or garbage collection.
Warmup helps, but it does not guarantee stable JVM performance. Counters add some
overhead. More seeds, independent density changes, and more repetitions would
make a broader comparison.

## Testing

Running `sh run.sh test` gives:

```text
All 199 checks passed.
All 6368 directed checks passed.
Experiment checks passed.
MST optimality checks passed for 30 graphs.
```

These are individual checks, not thousands of separate input graphs.

| Case | What is checked |
| --- | --- |
| Small known graph | Both MST algorithms return weight 6 |
| Independent MST optimality | Both weights match exhaustive enumeration on 30 six-vertex graphs |
| Disconnected graphs | Correct forest weight, edge count, and component count |
| Multiple MSTs and repeated weights | Equal-weight cycle gives total weight 3 |
| Single vertex | MST, SCC, topological ordering, and shortest paths |
| Directed cycles and self-loops | Topological sorting reports a cycle |
| SCC correctness | Each vertex occurs once; membership agrees with mutual reachability |
| Unreachable vertices | Distance is Long.MAX_VALUE and the path is empty |
| Shortest distances | Compared with Floyd-Warshall from every source |
| Reconstructed paths | Correct endpoints, existing edges, no repeated vertices, correct weight |
| Zero and negative weights | Zero-weight paths work; Dijkstra rejects negative edges |
| Parallel edges and self-loops | MST and shortest-path cases handle them correctly |
| Large totals | Sums of two maximum int weights use long correctly |
| Deep traversal | A 20000-vertex chain works without recursive DFS |
| Invalid input | Invalid vertices, sizes, sources, and directedness are rejected |
| Generators and counters | Exact sizes, seed reproducibility, connectivity, DAG/cycle status, scan identities |

DirectedTests also uses 20 seeded seven-vertex graphs. SCC results are compared
with independent transitive closure, and Dijkstra with Floyd-Warshall. MST tests
check acyclicity, selected-edge existence, component coverage, and weight sums,
in addition to agreement between Kruskal and Prim and known expected totals.
MSTOptimalityTests enumerates every five-edge subset of 30 six-vertex, nine-edge
graphs and uses BFS to check connectivity. This provides an independent minimum
weight without reusing the DSU or greedy algorithms. These graphs include repeated,
zero, and negative weights.

## Conclusion

Kruskal and Prim solve the infrastructure connection problem. Both give the same
minimum total weight on connected inputs and report a forest for disconnected
ones. SCC finds mutually reachable districts. Topological sorting handles acyclic
dependencies and reports cycles. Dijkstra finds routes with non-negative weights
and supports both unreachable vertices and path reconstruction.

The operation counts agree with the expected scans, sorting, and heap work.
The timing results depend on implementation details and JVM behavior, so the
algorithm should be chosen for the task and graph representation rather than
assuming one method is always fastest.
