package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.domain.FeedEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
public class FeedAdapter {

    private final RestTemplate restTemplate;

    private final String feedServiceUrl;

    public FeedAdapter(
            @Autowired final RestTemplate restTemplate,
            @Value("${service.interestFeed.url}") final String feedServiceUrl) {
        this.restTemplate = restTemplate;
        this.feedServiceUrl = feedServiceUrl;
    }

    public List<FeedEntry> getFeedByUser(final String username, final LocalDate date) {
        UriComponents feedUri = fromHttpUrl(feedServiceUrl)
                .path("feed/")
                .path(username)
                .queryParam("date", date)
                .build();
        return List.of(Objects.requireNonNull(restTemplate.getForObject(feedUri.toUri(), FeedEntry[].class)));
    }

    public List<String> getAllUsernames(final int limit) {
        UriComponents feedUri = fromHttpUrl(feedServiceUrl)
                .path("usernames")
                .queryParam("limit", limit)
                .build();
        return List.of(Objects.requireNonNull(restTemplate.getForObject(feedUri.toUri(), String[].class)));
    }
}
