package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.adapter.NewsAdapter;
import de.envite.pattern.caching.feed.adapter.NewsEntry;
import de.envite.pattern.caching.feed.config.FeedProperties;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class FeedService {

    private static final Comparator<FeedEntry> FEED_COMPARATOR = Comparator.comparing(FeedEntry::releaseDate).reversed().thenComparing(FeedEntry::topic);

    private final FeedProperties feedProperties;
    private final UserInterestRepository userInterestService;
    private final NewsAdapter newsAdapter;

    public FeedService(@Autowired final FeedProperties feedProperties,
                       @Autowired final UserInterestRepository userInterestRepository,
                       @Autowired final NewsAdapter newsAdapter) {
        this.feedProperties = feedProperties;
        this.userInterestService = userInterestRepository;
        this.newsAdapter = newsAdapter;
    }

    @Timed
    public List<FeedEntry> getFeedByUser(final String user, final LocalDate date) {
        final var feed = new ArrayList<FeedEntry>(feedProperties.getLimit());
        final var interests = userInterestService.getInterestsByUser(user);
        final var fromDate = date.minus(feedProperties.getPeriod());
        feed.addAll(toFeed(newsAdapter.getRecommendedNews(interests, fromDate, date, feedProperties.getLimit())));
        if (feed.size() < feedProperties.getLimit()) {
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
