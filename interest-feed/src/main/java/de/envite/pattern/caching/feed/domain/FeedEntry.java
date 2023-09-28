package de.envite.pattern.caching.feed.domain;

import java.io.Serializable;
import java.time.LocalDate;

public record FeedEntry(String articleUrl, String title, String topic, String description, String authors, LocalDate releaseDate) implements Serializable {}
