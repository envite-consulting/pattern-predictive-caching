package de.envite.pattern.caching.benchmark.adapter;

import java.io.Serializable;
import java.time.LocalDate;

public record FeedEntry(String articleUrl, String title, String topic, String description, String authors, LocalDate releaseDate) implements Serializable {}

