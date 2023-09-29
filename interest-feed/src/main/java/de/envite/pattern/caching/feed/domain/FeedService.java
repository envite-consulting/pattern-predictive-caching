package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.adapter.NewsAdapter;
import de.envite.pattern.caching.feed.config.FeedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class FeedService {

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

    public List<FeedEntry> getFeedByUser(final String user, final LocalDate date) {
        final Set<String> interests = userInterestService.getInterestsByUser(user);
        final LocalDate fromDate = date.minus(feedProperties.getPeriod());
        return newsAdapter.getRecommendedNews(interests, fromDate, date, feedProperties.getLimit()).newsEntries().stream()
                .map(r -> new FeedEntry(r.link(), r.headline(), r.category(), r.shortDescription(), r.authors(), LocalDate.parse(r.date())))
                .toList();
    }

}
