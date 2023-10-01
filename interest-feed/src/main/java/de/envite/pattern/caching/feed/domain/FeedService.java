package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.adapter.NewsAdapter;
import de.envite.pattern.caching.feed.adapter.NewsResponse;
import de.envite.pattern.caching.feed.config.FeedProperties;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        final List<FeedEntry> feed = new ArrayList<>(feedProperties.getLimit());
        final Set<String> interests = userInterestService.getInterestsByUser(user);
        final LocalDate fromDate = date.minus(feedProperties.getPeriod());
        feed.addAll(toFeed(newsAdapter.getRecommendedNews(interests, fromDate, date, feedProperties.getLimit())));
        if (feed.size() < feedProperties.getLimit()) {
            feed.addAll(toFeed(newsAdapter.getLatestNews(date, feedProperties.getLimit() - feed.size())));
        }
        feed.sort(FEED_COMPARATOR);
        return feed;
    }

    private List<FeedEntry> toFeed(final NewsResponse newsResponse) {
        return newsResponse.newsEntries().stream()
                .map(r -> new FeedEntry(r.link(), r.headline(), r.category(), r.shortDescription(), r.authors(), LocalDate.parse(r.date())))
                .toList();
    }
}
