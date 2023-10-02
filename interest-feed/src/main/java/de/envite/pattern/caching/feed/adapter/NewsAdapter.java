package de.envite.pattern.caching.feed.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
public class NewsAdapter {

    private final RestTemplate restTemplate;
    private final String recommendedNewsUriTemplate;
    private final String latestNewsUriTemplate;

    public NewsAdapter(
            @Autowired final RestTemplate restTemplate,
            @Value("${service.news.url}") final String newsServiceUrl) {
        this.restTemplate = restTemplate;
        this.recommendedNewsUriTemplate = fromHttpUrl(newsServiceUrl).path("recommendedNews").build().toUriString();
        this.latestNewsUriTemplate = fromHttpUrl(newsServiceUrl).path("latestNews").queryParam("untilDate","{untilDate}").queryParam("limit", "{limit}").build().toUriString();
    }

    public List<NewsEntry> getRecommendedNews(final Set<String> topics, final LocalDate fromDate, final LocalDate untilDate, final Integer limit) {
        final var newsResponse = restTemplate.postForObject(recommendedNewsUriTemplate, new RecommendedNewsQuery(topics, fromDate, untilDate, limit), NewsResponse.class);
        return ofNullable(newsResponse).map(NewsResponse::newsEntries).orElseGet(Collections::emptyList);
    }

    public List<NewsEntry> getLatestNews(final LocalDate untilDate, final Integer limit) {
        final var uriVariables = new HashMap<String, String>();
        ofNullable(untilDate).ifPresent(v -> uriVariables.put("untilDate", v.toString()));
        ofNullable(limit).ifPresent(v -> uriVariables.put("limit", Integer.toString(v)));
        final var newsResponse = restTemplate.getForObject(latestNewsUriTemplate, NewsResponse.class, uriVariables);
        return ofNullable(newsResponse).map(NewsResponse::newsEntries).orElseGet(Collections::emptyList);
    }
}
