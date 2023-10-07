package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.support.CloseableRestOperations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.io.InterruptedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

public class FeedAdapter implements AutoCloseable {

    private static final ParameterizedTypeReference<List<FeedEntry>> LIST_FEEDENTRY = new ParameterizedTypeReference<>(){};
    private static final ParameterizedTypeReference<List<String>> LIST_STRING_TYPE = new ParameterizedTypeReference<>(){};

    private final CloseableRestOperations restOperations;
    private final String feedUriTemplate;
    private final String usernamesUriTemplate;

    public FeedAdapter(final RestOperationsFactory restOperationsFactory, final String feedServiceUrl) {
        this(restOperationsFactory.createRestOperations(), feedServiceUrl);
    }

    FeedAdapter(final CloseableRestOperations restOperations, final String feedServiceUrl) {
        this.restOperations = restOperations;
        this.feedUriTemplate = fromHttpUrl(feedServiceUrl).path("feed/{username}").queryParam("date", "{date}").build().toUriString();
        this.usernamesUriTemplate = fromHttpUrl(feedServiceUrl).path("usernames").queryParam("limit", "{limit}").build().toUriString();
    }

    public List<FeedEntry> getFeedByUser(final String username, final LocalDate date) throws InterruptedException {
        return exchange(feedUriTemplate, GET, null, LIST_FEEDENTRY,
                        Map.of("username", username, "date", date.toString())).getBody();
    }

    public List<String> getAllUsernames(final int limit) throws InterruptedException {
        return exchange(usernamesUriTemplate, GET, null, LIST_STRING_TYPE,
                        Map.of("limit", Integer.toString(limit))).getBody();
    }

    @Override
    public void close() {
        restOperations.close();
    }

    private <T> ResponseEntity<T> exchange(String uriTemplate, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws InterruptedException {
        try {
            return restOperations.exchange(uriTemplate, method, requestEntity, responseType, uriVariables);
        } catch (final ResourceAccessException e) {
            if (e.getCause() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
                throw new InterruptedException(String.format("Interrupted during calling %s %s", method, uriTemplate));
            }
            throw e;
        }
    }
}
