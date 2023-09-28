package de.envite.pattern.caching.news.domain;

import de.envite.pattern.caching.news.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.news.adapter.RecommendedNewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class NewsService {

    private NewsRepository newsRepository;

    @Autowired
    NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public RecommendedNewsResponse getRecommendedNews(RecommendedNewsQuery query) {
        // get all entries in certain category
        LocalDate start = LocalDate.ofInstant(query.getStart(), ZoneId.of("Europe/Berlin"));
        LocalDate end = LocalDate.ofInstant(query.getEnd(), ZoneId.of("Europe/Berlin"));

        List<NewsEntry> entries = newsRepository.findNewsEntriesByCategoryInAndDateGreaterThanEqualAndDateLessThanEqual(query.getTopics(), start, end);

        List<NewsEntry> responseEntries = new ArrayList<>();
        if(entries.size() > 0) {
            // randomly select entries according to the specified limit
            Random rand = new Random();
            for(int i = 0; i < query.getLimit(); i++) {
                NewsEntry randomElement = entries.get(rand.nextInt(entries.size()));
                responseEntries.add(randomElement);
            }
        }
        return new RecommendedNewsResponse(responseEntries);
    }
}
