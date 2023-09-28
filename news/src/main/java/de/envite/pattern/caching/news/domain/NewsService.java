package de.envite.pattern.caching.news.domain;

import de.envite.pattern.caching.news.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.news.adapter.NewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public NewsResponse getRecommendedNews(RecommendedNewsQuery query) {
        LocalDate startTime = LocalDate.ofInstant(query.getStartTime(), ZoneId.of("Europe/Berlin"));
        LocalDate endTime = LocalDate.ofInstant(query.getEndTime(), ZoneId.of("Europe/Berlin"));

        List<NewsEntry> entries = newsRepository.findNewsEntriesByCategoryInAndDateGreaterThanEqualAndDateLessThanEqual(query.getTopics(), startTime, endTime);
        return new NewsResponse(getRandomNewsEntries(entries, query.getLimit()));
    }

    public NewsResponse getLatestNews(int count, Instant endTime) {
        Pageable top = PageRequest.of(0, count);
        List<NewsEntry> entries = newsRepository.findByDateLessThanEqual(LocalDate.ofInstant(endTime, ZoneId.of("Europe/Berlin")), top);
        return new NewsResponse(entries);
    }

    private List<NewsEntry> getRandomNewsEntries(List<NewsEntry> entries, int count) {
        List<NewsEntry> responseEntries = new ArrayList<>();
        if(entries.size() > 0) {
            // randomly select entries according to the specified limit
            Random rand = new Random();
            for(int i = 0; i < count; i++) {
                NewsEntry randomElement = entries.get(rand.nextInt(entries.size()));
                responseEntries.add(randomElement);
            }
        }
        return responseEntries;
    }
}
