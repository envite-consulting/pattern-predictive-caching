package de.envite.pattern.caching.feed.web;

import de.envite.pattern.caching.feed.domain.FeedEntry;
import de.envite.pattern.caching.feed.domain.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/feed")
public class FeedResource {


    private final FeedService feedService;

    public FeedResource(@Autowired final FeedService feedService) {
        this.feedService = feedService;
    }

    @RequestMapping(method = GET, path = "/{user}")
    public List<FeedEntry> getFeedByUser(@PathVariable final String user, @RequestParam(name = "date", required = false) final LocalDate date) {
        return feedService.getFeedByUser(user, Optional.ofNullable(date).orElseGet(LocalDate::now));
    }
}
