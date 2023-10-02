package de.envite.pattern.caching.news.web;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record RecommendedNewsQuery(List<String> topics, LocalDate fromDate, LocalDate untilDate, Integer limit) implements Serializable {}
