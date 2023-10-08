package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.adapter.NewsAdapter;
import de.envite.pattern.caching.feed.adapter.NewsEntry;
import de.envite.pattern.caching.feed.config.FeedProperties;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static de.envite.pattern.caching.feed.support.MetricsSupport.toTags;
import static java.util.Optional.ofNullable;

@Service
public class FeedService {

    private static final Comparator<FeedEntry> FEED_COMPARATOR = Comparator.comparing(FeedEntry::releaseDate).reversed().thenComparing(FeedEntry::topic);

    private final FeedProperties feedProperties;
    private final UserInterestRepository userInterestService;
    private final NewsAdapter newsAdapter;

    private final DistributionSummary summaryFeedRecommendedNewsSize;

    @Autowired
    public FeedService(final FeedProperties feedProperties,
                       final UserInterestRepository userInterestRepository,
                       final NewsAdapter newsAdapter,
                       final MeterRegistry meterRegistry, @Autowired final MetricsProperties metricsProperties) {
        this.feedProperties = feedProperties;
        this.userInterestService = userInterestRepository;
        this.newsAdapter = newsAdapter;

        this.summaryFeedRecommendedNewsSize = meterRegistry.summary("feed.recommended.news.size", toTags(metricsProperties.getTags()));
    }

    @Timed
    public List<FeedEntry> getFeedByUser(final String user, final LocalDate date) {
        final var feed = new ArrayList<FeedEntry>(feedProperties.getLimit());
        if (feedProperties.isUseRecommendedNews()) {
            final var interests = userInterestService.getInterestsByUser(user);
            final var fromDate = date.minus(feedProperties.getPeriod());
            feed.addAll(toFeed(newsAdapter.getRecommendedNews(interests, fromDate, date, feedProperties.getLimit())));
        }
        summaryFeedRecommendedNewsSize.record(feed.size());
        if (feed.size() < feedProperties.getLimit() && feedProperties.isUseLatestNews()) {
            feed.addAll(toFeed(newsAdapter.getLatestNews(date, feedProperties.getLimit() - feed.size())));
        }
        feed.sort(FEED_COMPARATOR);
        return feed;
    }

    private List<FeedEntry> toFeed(final List<NewsEntry> newsEntries) {
        return newsEntries.stream()
                .map(r -> new FeedEntry(r.link(), r.headline(), r.category(), r.shortDescription(), r.authors(), ofNullable(r.date()).map(LocalDate::parse).orElse(null)))
                .toList();
    }

}
