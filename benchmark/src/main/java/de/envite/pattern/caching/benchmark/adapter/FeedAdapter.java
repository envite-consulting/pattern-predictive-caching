package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.domain.FeedEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
public class FeedAdapter {

    private static final ParameterizedTypeReference<List<FeedEntry>> LIST_FEEDENTRY = new ParameterizedTypeReference<List<FeedEntry>>(){};
    private static final ParameterizedTypeReference<List<String>> LIST_STRING_TYPE = new ParameterizedTypeReference<List<String>>(){};

    private final RestTemplate restTemplate;
    private final String feedUriTemplate;
    private final String usernamesUriTemplate;

    public FeedAdapter(
            @Autowired final RestTemplate restTemplate,
            @Value("${service.interestFeed.url}") final String feedServiceUrl) {
        this.restTemplate = restTemplate;
        this.feedUriTemplate = fromHttpUrl(feedServiceUrl).path("feed/{username}").queryParam("date", "{date}").build().toUriString();
        this.usernamesUriTemplate = fromHttpUrl(feedServiceUrl).path("usernames").queryParam("limit", "{limit}").build().toUriString();
    }

    public List<FeedEntry> getFeedByUser(final String username, final LocalDate date) {
        return restTemplate.exchange(
                feedUriTemplate, GET, null, LIST_FEEDENTRY,
                Map.of("username", username, "date", date.toString())).getBody();
    }

    public List<String> getAllUsernames(final int limit) {
        return restTemplate.exchange(
                usernamesUriTemplate, GET, null, LIST_STRING_TYPE,
                Map.of("limit", Integer.toString(limit))).getBody();
    }
}
