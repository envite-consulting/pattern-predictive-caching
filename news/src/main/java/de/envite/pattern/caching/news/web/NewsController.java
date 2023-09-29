package de.envite.pattern.caching.news.web;

import de.envite.pattern.caching.news.domain.NewsService;
import de.envite.pattern.caching.news.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.news.adapter.NewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping(value ="/")
public class NewsController {

    private NewsService newsService;
    @Autowired
    NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping(value = "/recommendedNews", consumes = "application/json", produces = "application/json")
    public NewsResponse getRecommendedNews(@RequestBody RecommendedNewsQuery query) {
        return this.newsService.getRecommendedNews(query);
    }

    @GetMapping(value = "/latestNews", produces = "application/json")
    public NewsResponse getLatestNews(@RequestParam int limit, @RequestParam LocalDate untilDate) {
        return this.newsService.getLatestNews(limit, untilDate);
    }
}
