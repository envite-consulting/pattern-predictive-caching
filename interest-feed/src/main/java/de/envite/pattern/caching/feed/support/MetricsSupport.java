package de.envite.pattern.caching.feed.support;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.Map;

import static java.util.stream.Collectors.toSet;

public final class MetricsSupport {

    private MetricsSupport() {}

    public static Tags toTags(final Map<String, String> tags) {
        return Tags.of(tags.entrySet().stream()
                .map(e -> Tag.of(e.getKey(), e.getValue()))
                .collect(toSet()));
    }
}
