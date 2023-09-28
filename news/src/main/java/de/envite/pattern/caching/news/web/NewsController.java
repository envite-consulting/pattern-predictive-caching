package de.envite.pattern.caching.news.web;

import de.envite.pattern.caching.news.domain.NewsService;
import de.envite.pattern.caching.news.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.news.adapter.RecommendedNewsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value ="/")
public class NewsController {

    private NewsService newsService;
    @Autowired
    NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping(value = "/recommendedNews", consumes = "application/json", produces = "application/json")
    public RecommendedNewsResponse getRecommendedNews(@RequestBody RecommendedNewsQuery query) {
        return this.newsService.getRecommendedNews(query);
    }
}
