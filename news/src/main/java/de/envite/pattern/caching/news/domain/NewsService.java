package de.envite.pattern.caching.news.domain;

import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.shuffle;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(final NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Timed
    public List<NewsEntry> getRecommendedNews(final List<String> topics, final LocalDate fromDate, final LocalDate untilDate, final int limit) {
        final List<NewsEntry> entries = newsRepository.findByCategoryInAndDateGreaterThanEqualAndDateLessThanEqual(topics, fromDate, untilDate);
        return getRandomNewsEntries(entries, limit);
    }

    @Timed
    public List<NewsEntry> getLatestNews(final LocalDate untilDate, final int limit) {
        final Pageable top = PageRequest.of(0, limit);
        return newsRepository.findByDateLessThanEqual(untilDate, top);
    }

    private List<NewsEntry> getRandomNewsEntries(final List<NewsEntry> entries, final int count) {
        if(entries.size() > count) {
            shuffle(entries, ThreadLocalRandom.current());
            return entries.subList(0, count);
        } else {
            return entries;
        }
    }
}
