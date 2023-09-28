package de.envite.pattern.caching.feed.domain;

import de.envite.pattern.caching.feed.adapter.NewsAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptySet;

@Component
public class FeedService {

    private final NewsAdapter newsAdapter;

    public FeedService(@Autowired final NewsAdapter newsAdapter) {
        this.newsAdapter = newsAdapter;
    }

    public List<FeedEntry> getFeedByUser(final String user, final LocalDate date) {
        return newsAdapter.getRecommendedNews(emptySet(), Instant.now().minusMillis(TimeUnit.DAYS.toMillis(365)), date.atTime(OffsetTime.now()).toInstant(), 100).stream()
                .map(r -> new FeedEntry(r.link(), r.headline(), r.category(), r.short_description(), r.authors(), LocalDate.parse(r.date())))
                .toList();
    }

}
