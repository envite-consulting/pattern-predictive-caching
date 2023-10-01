package de.envite.pattern.caching.feed.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

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

    public NewsResponse getRecommendedNews(final Set<String> topics, final LocalDate fromDate, final LocalDate untilDate, final int limit) {
        return restTemplate.postForObject(recommendedNewsUriTemplate, new RecommendedNewsQuery(topics, fromDate, untilDate, limit), NewsResponse.class);
    }

    public NewsResponse getLatestNews(final LocalDate untilDate, final int limit) {
        return restTemplate.getForObject(latestNewsUriTemplate, NewsResponse.class, Map.of("untilDate", untilDate.toString(), "limit", Integer.toString(limit)));
    }
}
