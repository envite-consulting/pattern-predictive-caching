package de.envite.pattern.caching.news.controller;

import de.envite.pattern.caching.news.domain.NewsEntry;

import java.io.Serializable;
import java.util.List;

public record NewsResponse(List<NewsEntry> newsEntries) implements Serializable {}
