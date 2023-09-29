package de.envite.pattern.caching.feed.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
public class NewsAdapter {

    private final RestTemplate restTemplate;
    private final UriComponents recommendedNewsUri;

    public NewsAdapter(
            @Autowired final RestTemplate restTemplate,
            @Value("${service.news.url}") final String newsServiceUrl) {
        this.restTemplate = restTemplate;
        this.recommendedNewsUri = fromHttpUrl(newsServiceUrl).path("recommendedNews").build();
    }

    public RecommendedNews getRecommendedNews(final Set<String> topics, final LocalDate fromDate, final LocalDate untilDate, final int limit) {
        return restTemplate.postForObject(recommendedNewsUri.toUri(), new RecommendedNewsQuery(topics, fromDate, untilDate, limit), RecommendedNews.class);
    }
}
