package de.envite.pattern.caching.benchmark.adapter;

import static java.util.Objects.requireNonNull;

public class FeedAdapterFactory {

    private final RestOperationsFactory restOperationsFactory;
    private final String feedServiceUrl;

    public FeedAdapterFactory(final RestOperationsFactory restOperationsFactory,
                              final String feedServiceUrl) {
        this.restOperationsFactory = requireNonNull(restOperationsFactory);
        this.feedServiceUrl = requireNonNull(feedServiceUrl);
    }

    public FeedAdapter createFeedAdapter() {
        return new FeedAdapter(restOperationsFactory, feedServiceUrl);
    }
}
