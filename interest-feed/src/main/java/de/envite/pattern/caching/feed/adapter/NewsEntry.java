package de.envite.pattern.caching.feed.adapter;

import java.io.Serializable;

public record NewsEntry(int id, String link, String headline, String category, String shortDescription, String authors, String date) implements Serializable {}