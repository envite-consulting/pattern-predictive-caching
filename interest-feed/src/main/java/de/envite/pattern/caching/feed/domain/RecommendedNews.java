package de.envite.pattern.caching.feed.domain;

import java.io.Serializable;

public record RecommendedNews(String link, String headline, String category, String short_description, String authors, String date) implements Serializable {}
