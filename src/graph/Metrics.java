package graph;

public class Metrics {
    public long edgeChecks;
    public long comparisons;
    public long findCalls;
    public long unionCalls;
    public long parentHops;
    public long queueOffers;
    public long queuePolls;

    public String toCsv() {
        return edgeChecks + "," + comparisons + "," + findCalls + "," + unionCalls
                + "," + parentHops + "," + queueOffers + "," + queuePolls;
    }
}
