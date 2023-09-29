package de.envite.pattern.caching.news.adapter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class RecommendedNewsQuery {
    private List<String> topics;
    private int limit;
    private LocalDate fromDate;
    private LocalDate untilDate;


    public RecommendedNewsQuery() {
    }

    public RecommendedNewsQuery(List<String> topics, int limit, LocalDate fromDate, LocalDate untilDate) {
        this.topics = topics;
        this.limit = limit;
        this.fromDate = fromDate;
        this.untilDate = untilDate;
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

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(LocalDate untilDate) {
        this.untilDate = untilDate;
    }
}
