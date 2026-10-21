package graph;

/** Per-run counters. A comparison is one comparator invocation. */
public final class Metrics {
    public long edgeExaminations, comparisons, findCalls, parentHops, unionCalls, queueOffers, queuePolls;
    public String csv() {
        return edgeExaminations + "," + comparisons + "," + findCalls + "," + parentHops
                + "," + unionCalls + "," + queueOffers + "," + queuePolls;
    }
}
