package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.config.OkHttpClientProperties;
import de.envite.pattern.caching.benchmark.support.CloseableRestOperations;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class RestOperationsFactory {

    private final OkHttpClientProperties okHttpclientProperties;
    private final HttpMessageConverters messageConverters;

    public RestOperationsFactory(final OkHttpClientProperties okHttpclientProperties,
                                 final HttpMessageConverters messageConverters) {
        this.okHttpclientProperties = requireNonNull(okHttpclientProperties);
        this.messageConverters = requireNonNull(messageConverters);
    }

    public CloseableRestOperations createRestOperations() {
        final var closeables = new LinkedList<AutoCloseable>();
        final var restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(messageConverters.getConverters());
        if (okHttpclientProperties.isEnabled()) {
            final var httpClient = okHttpClient();
            closeables.add(() -> httpClient.connectionPool().evictAll());
            restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(httpClient));
        }
        return new CloseableRestOperations(restTemplate, closeables);
    }

    private OkHttpClient okHttpClient() {
        final var connectionPool = new ConnectionPool(okHttpclientProperties.getPool().getMaxIdleConnections(), okHttpclientProperties.getPool().getKeepAliveDuration().toMillis(), MILLISECONDS);
        return new OkHttpClient.Builder()
                .protocols(okHttpclientProperties.getProtocols())
                .connectTimeout(okHttpclientProperties.getConnectTimeout())
                .readTimeout(okHttpclientProperties.getReadTimeout())
                .writeTimeout(okHttpclientProperties.getWriteTimeout())
                .callTimeout(okHttpclientProperties.getCallTimeout())
                .connectionPool(connectionPool)
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }

}
