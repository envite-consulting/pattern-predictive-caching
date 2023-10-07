package de.envite.pattern.caching.benchmark.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class CloseableRestOperations implements RestOperations, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CloseableRestOperations.class);

    private final RestOperations delegate;
    private final List<AutoCloseable> closeables;

    public CloseableRestOperations(final RestOperations delegate, final List<AutoCloseable> closeables) {
        this.delegate = requireNonNull(delegate);
        this.closeables = requireNonNull(closeables);
    }

    @Override
    public void close() {
        final var exceptions = new ArrayList<Exception>(closeables.size());
        for (final AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (final InterruptedException e) {
                exceptions.add(e);
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            log.warn("Closing of rest operations failed!", exceptions.get(0));
        }
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.getForObject(url, responseType, uriVariables);
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.getForObject(url, responseType, uriVariables);
    }

    @Override
    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        return delegate.getForObject(url, responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.getForEntity(url, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.getForEntity(url, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        return delegate.getForEntity(url, responseType);
    }

    @Override
    public HttpHeaders headForHeaders(String url, Object... uriVariables) throws RestClientException {
        return delegate.headForHeaders(url, uriVariables);
    }

    @Override
    public HttpHeaders headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.headForHeaders(url, uriVariables);
    }

    @Override
    public HttpHeaders headForHeaders(URI url) throws RestClientException {
        return delegate.headForHeaders(url);
    }

    @Override
    public URI postForLocation(String url, Object request, Object... uriVariables) throws RestClientException {
        return delegate.postForLocation(url, request, uriVariables);
    }

    @Override
    public URI postForLocation(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.postForLocation(url, request, uriVariables);
    }

    @Override
    public URI postForLocation(URI url, Object request) throws RestClientException {
        return delegate.postForLocation(url, request);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.postForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return delegate.postForObject(url, request, responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.postForEntity(url, request, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.postForEntity(url, request, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
        return delegate.postForEntity(url, request, responseType);
    }

    @Override
    public void put(String url, Object request, Object... uriVariables) throws RestClientException {
        delegate.put(url, request, uriVariables);
    }

    @Override
    public void put(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
        delegate.put(url, request, uriVariables);
    }

    @Override
    public void put(URI url, Object request) throws RestClientException {
        delegate.put(url, request);
    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.patchForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T patchForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.patchForObject(url, request, responseType, uriVariables);
    }

    @Override
    public <T> T patchForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
        return delegate.patchForObject(url, request, responseType);
    }

    @Override
    public void delete(String url, Object... uriVariables) throws RestClientException {
        delegate.delete(url, uriVariables);
    }

    @Override
    public void delete(String url, Map<String, ?> uriVariables) throws RestClientException {
        delegate.delete(url, uriVariables);
    }

    @Override
    public void delete(URI url) throws RestClientException {
        delegate.delete(url);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Object... uriVariables) throws RestClientException {
        return delegate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.optionsForAllow(url, uriVariables);
    }

    @Override
    public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
        return delegate.optionsForAllow(url);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType, uriVariables);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return delegate.exchange(url, method, requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        return delegate.exchange(requestEntity, responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return delegate.exchange(requestEntity, responseType);
    }

    @Override
    public <T> T execute(String uriTemplate, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... uriVariables) throws RestClientException {
        return delegate.execute(uriTemplate, method, requestCallback, responseExtractor, uriVariables);
    }

    @Override
    public <T> T execute(String uriTemplate, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables) throws RestClientException {
        return delegate.execute(uriTemplate, method, requestCallback, responseExtractor, uriVariables);
    }

    @Override
    public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        return delegate.execute(url, method, requestCallback, responseExtractor);
    }
}
