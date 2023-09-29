package de.envite.pattern.caching.news.domain;

import de.envite.pattern.caching.news.adapter.NewsResponse;
import de.envite.pattern.caching.news.adapter.RecommendedNewsQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        List<NewsEntry> entries = newsRepository.findNewsEntriesByCategoryInAndDateGreaterThanEqualAndDateLessThanEqual(
                query.getTopics(),
                query.getFromDate(),
                query.getUntilDate()
        );
        return new NewsResponse(getRandomNewsEntries(entries, query.getLimit()));
    }

    public NewsResponse getLatestNews(int limit, LocalDate untilDate) {
        Pageable top = PageRequest.of(0, limit);
        List<NewsEntry> entries = newsRepository.findByDateLessThanEqual(untilDate, top);
        return new NewsResponse(entries);
    }

    private List<NewsEntry> getRandomNewsEntries(List<NewsEntry> entries, int count) {
        List<NewsEntry> responseEntries = new ArrayList<>();
        if(entries.size() > 0) {
            Random rand = new Random();
            for(int i = 0; i < count; i++) {
                NewsEntry randomElement = entries.get(rand.nextInt(entries.size()));
                responseEntries.add(randomElement);
            }
        }
        return responseEntries;
    }
}
