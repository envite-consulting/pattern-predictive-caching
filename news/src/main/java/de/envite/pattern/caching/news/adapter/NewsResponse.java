package de.envite.pattern.caching.news.adapter;

import de.envite.pattern.caching.news.domain.NewsEntry;

import java.util.List;

public class NewsResponse {
    List<NewsEntry> newsEntries;

    public NewsResponse() {
    }
    public NewsResponse(List<NewsEntry> newsEntries) {
        this.newsEntries = newsEntries;
    }

    public List<NewsEntry> getNewsEntries() {
        return newsEntries;
    }

    public void setNewsEntries(List<NewsEntry> newsEntries) {
        this.newsEntries = newsEntries;
    }
}
