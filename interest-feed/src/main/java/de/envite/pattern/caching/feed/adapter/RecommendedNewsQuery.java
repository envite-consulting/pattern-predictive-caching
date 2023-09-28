package de.envite.pattern.caching.feed.adapter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

public record RecommendedNewsQuery(Set<String> topics, Instant startTime, Instant endTime, int limit) implements Serializable {}
