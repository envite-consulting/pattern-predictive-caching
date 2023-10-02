package de.envite.pattern.caching.news.web;

import de.envite.pattern.caching.news.domain.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(value ="/")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(final NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping(value = "/recommendedNews")
    public NewsResponse getRecommendedNews(@RequestBody final RecommendedNewsQuery query) {
        return new NewsResponse(newsService.getRecommendedNews(query.topics(),
                ofNullable(query.fromDate()).orElseGet(LocalDate::now), ofNullable(query.untilDate()).orElseGet(LocalDate::now),
                ofNullable(query.limit()).orElse(Integer.MAX_VALUE)));
    }

    @GetMapping(value = "/latestNews")
    public NewsResponse getLatestNews(@RequestParam(name = "untilDate", required = false) final LocalDate untilDate,
                                      @RequestParam(name = "limit", required = false) final Integer limit) {
        return new NewsResponse(newsService.getLatestNews(ofNullable(untilDate).orElseGet(LocalDate::now), ofNullable(limit).orElse(Integer.MAX_VALUE)));
    }
}
