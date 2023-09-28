package de.envite.pattern.caching.feed.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
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

    public List<RecommendedNews> getRecommendedNews(final Set<String> topics, final Instant startTime, final Instant endTime, final int limit) {
        final RecommendedNews[] recommendedNews =
                restTemplate.postForObject(recommendedNewsUri.toUri(), new RecommendedNewsQuery(topics, startTime, endTime, limit), RecommendedNews[].class);
        return recommendedNews != null ? List.of(recommendedNews) : emptyList();
    }
}
