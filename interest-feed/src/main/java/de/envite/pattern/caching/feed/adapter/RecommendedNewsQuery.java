package de.envite.pattern.caching.feed.adapter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

public record RecommendedNewsQuery(Set<String> topics, LocalDate fromDate, LocalDate untilDate, Integer limit) implements Serializable {}
