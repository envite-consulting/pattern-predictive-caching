package de.envite.pattern.caching.news.adapter;

import java.time.Instant;
import java.util.List;

public class RecommendedNewsQuery {
    private List<String> topics;
    private int limit;
    private Instant startTime;
    private Instant endTime;


    public RecommendedNewsQuery() {
    }

    public RecommendedNewsQuery(List<String> topics, int limit, Instant startTime, Instant endTime) {
        this.topics = topics;
        this.limit = limit;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
}
