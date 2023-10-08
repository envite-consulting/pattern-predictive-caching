package de.envite.pattern.caching.feed.controller;

import de.envite.pattern.caching.feed.config.WebResourceProperties;
import de.envite.pattern.caching.feed.domain.FeedEntry;
import de.envite.pattern.caching.feed.domain.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static de.envite.pattern.caching.feed.support.ResponseEntitySupport.ok;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.springframework.http.CacheControl.maxAge;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private final WebResourceProperties.Feed.CacheControl cacheControlProperties;
    private final FeedService feedService;

    @Autowired
    public FeedController(final WebResourceProperties webResourceProperties,
                          final FeedService feedService) {
        this.cacheControlProperties = webResourceProperties.getFeed().getCacheControl();
        this.feedService = feedService;
    }

    @GetMapping({"/{username}", "/", ""})
    public ResponseEntity<List<FeedEntry>> getFeedByUser(@PathVariable(name = "username", required = false) final String username,
                                                        @RequestParam(name = "date", required = false) final LocalDate date) {
        final var feed = feedService.getFeedByUser(ofNullable(username).orElse(""), ofNullable(date).orElseGet(LocalDate::now));
        return ok(feed, entry(cacheControlProperties::isEnabled, builder -> builder.cacheControl(maxAge(cacheControlProperties.getByUser()))));
    }
}
