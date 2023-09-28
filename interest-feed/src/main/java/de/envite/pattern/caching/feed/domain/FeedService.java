package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.config.FeedProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Component
public class FeedService {

    private final FeedProperties feedProperties;
    private final UserInterestService userInterestService;
    private final NewsService newsService;

    public FeedService(@Autowired final FeedProperties feedProperties,
                       @Autowired final UserInterestService userInterestService,
                       @Autowired final NewsService newsService) {
        this.feedProperties = feedProperties;
        this.userInterestService = userInterestService;
        this.newsService = newsService;
    }

    public List<FeedEntry> getFeedByUser(final String user, final LocalDate date) {
        final Set<String> interests = userInterestService.getInterestsByUser(user);
        final Instant endTime = date.atTime(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC)).toInstant();
        final Instant startTime = endTime.minus(feedProperties.getPeriod());
        return newsService.getRecommendedNews(interests, startTime, endTime, feedProperties.getLimit()).stream()
                .map(r -> new FeedEntry(r.link(), r.headline(), r.category(), r.short_description(), r.authors(), LocalDate.parse(r.date())))
                .toList();
    }

}
