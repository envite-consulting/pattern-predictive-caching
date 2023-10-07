package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.config.OkHttpClientProperties;
import de.envite.pattern.caching.benchmark.support.CloseableRestOperations;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.LinkedList;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class RestOperationsFactory {

    private final RestTemplateBuilder restTemplateBuilder;
    private final OkHttpClientProperties okHttpclientProperties;

    public RestOperationsFactory(final RestTemplateBuilder restTemplateBuilder,
                                 final OkHttpClientProperties okHttpclientProperties) {
        this.restTemplateBuilder = requireNonNull(restTemplateBuilder);
        this.okHttpclientProperties = requireNonNull(okHttpclientProperties);
    }

    public CloseableRestOperations createRestOperations() {
        final var restTemplate = restTemplateBuilder.detectRequestFactory(false).build();
        final var closeables = new LinkedList<AutoCloseable>();
        if (okHttpclientProperties.isEnabled()) {
            final var httpClient = okHttpClient();
            closeables.add(() -> httpClient.connectionPool().evictAll());
            restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(httpClient));
        } else {
            restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
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
