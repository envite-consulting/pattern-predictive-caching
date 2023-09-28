package de.envite.pattern.caching.feed.adapter;

import java.io.Serializable;
import java.util.List;

public record RecommendedNews(List<NewsEntry> newsEntries) implements Serializable {}
