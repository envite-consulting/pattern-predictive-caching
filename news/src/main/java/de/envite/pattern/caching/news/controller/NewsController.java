package de.envite.pattern.caching.news.controller;

import de.envite.pattern.caching.news.config.WebResourceProperties;
import de.envite.pattern.caching.news.domain.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static de.envite.pattern.caching.news.support.ResponseEntitySupport.ok;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.springframework.http.CacheControl.maxAge;

@RestController
@RequestMapping(value ="/news")
public class NewsController {

    private final WebResourceProperties.News.CacheControl cacheControlProperties;
    private final NewsService newsService;

    @Autowired
    public NewsController(final WebResourceProperties webResourceProperties,
                          final NewsService newsService) {
        this.cacheControlProperties = webResourceProperties.getNews().getCacheControl();
        this.newsService = newsService;
    }

    @GetMapping("/recommended")
    public ResponseEntity<NewsResponse> getRecommendedNews(@RequestParam(name = "topics", required = false) final Set<String> topics,
                                                           @RequestParam(name = "fromDate", required = false) final LocalDate fromDate,
                                                           @RequestParam(name = "untilDate", required = false) final LocalDate untilDate,
                                                           @RequestParam(name = "limit", required = false) final Integer limit) {
        final var news = newsService.getRecommendedNews(
                ofNullable(topics).orElseGet(Collections::emptySet),
                ofNullable(fromDate).orElseGet(LocalDate::now), ofNullable(untilDate).orElseGet(LocalDate::now),
                ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ok(new NewsResponse(news),
                entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getRecommended()))));
    }

    @GetMapping("/latest")
    public ResponseEntity<NewsResponse> getLatestNews(@RequestParam(name = "untilDate", required = false) final LocalDate untilDate,
                                                      @RequestParam(name = "limit", required = false) final Integer limit) {
        final var news = newsService.getLatestNews(ofNullable(untilDate).orElseGet(LocalDate::now), ofNullable(limit).orElse(Integer.MAX_VALUE));
        return ok(new NewsResponse(news),
                entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getLatest()))));
    }

}
