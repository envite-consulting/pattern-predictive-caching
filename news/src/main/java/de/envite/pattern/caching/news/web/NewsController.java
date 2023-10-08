package de.envite.pattern.caching.news.web;

import de.envite.pattern.caching.news.domain.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(value ="/")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(final NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/recommendedNews")
    public ResponseEntity<NewsResponse> getRecommendedNews(@RequestParam(name = "topics", required = false) final Set<String> topics,
                                                           @RequestParam(name = "fromDate", required = false) final LocalDate fromDate,
                                                           @RequestParam(name = "untilDate", required = false) final LocalDate untilDate,
                                                           @RequestParam(name = "limit", required = false) final Integer limit) {
        final var news = newsService.getRecommendedNews(
                ofNullable(topics).orElseGet(Collections::emptySet),
                ofNullable(fromDate).orElseGet(LocalDate::now), ofNullable(untilDate).orElseGet(LocalDate::now),
                ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
                .body(new NewsResponse(news));
    }

    @GetMapping("/latestNews")
    public ResponseEntity<NewsResponse> getLatestNews(@RequestParam(name = "untilDate", required = false) final LocalDate untilDate,
                                                      @RequestParam(name = "limit", required = false) final Integer limit) {
        final var news = newsService.getLatestNews(ofNullable(untilDate).orElseGet(LocalDate::now), ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)))
                .body(new NewsResponse(news));
    }
}
