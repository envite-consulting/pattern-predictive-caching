package de.envite.pattern.caching.news.model;

import java.util.List;

public class RecommendedNewsResponse {
    List<NewsEntry> newsEntries;

    public RecommendedNewsResponse() {
    }
    public RecommendedNewsResponse(List<NewsEntry> newsEntries) {
        this.newsEntries = newsEntries;
    }

    public List<NewsEntry> getNewsEntries() {
        return newsEntries;
    }

    public void setNewsEntries(List<NewsEntry> newsEntries) {
        this.newsEntries = newsEntries;
    }
}
