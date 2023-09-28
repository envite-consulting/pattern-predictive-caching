package de.envite.pattern.caching.news.adapter;

import java.time.Instant;
import java.util.List;

public class RecommendedNewsQuery {
    private List<String> topics;
    private int limit;
    private Instant start;
    private Instant end;


    public RecommendedNewsQuery() {
    }

    public RecommendedNewsQuery(List<String> topics, int limit, Instant start, Instant end) {
        this.topics = topics;
        this.limit = limit;
        this.start = start;
        this.end = end;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }
}
