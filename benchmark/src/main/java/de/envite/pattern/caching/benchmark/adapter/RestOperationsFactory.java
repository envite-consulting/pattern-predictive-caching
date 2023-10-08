package de.envite.pattern.caching.benchmark.adapter;

import de.envite.pattern.caching.benchmark.config.OkHttpClientProperties;
import de.envite.pattern.caching.benchmark.support.CloseableRestOperations;
import de.envite.pattern.caching.benchmark.support.OkHttpCacheSetMetrics;
import de.envite.pattern.caching.benchmark.support.OkHttpConnectionPoolSetMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

import static de.envite.pattern.caching.benchmark.support.MetricsSupport.toTags;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

public class RestOperationsFactory {

    private final RestTemplateBuilder restTemplateBuilder;
    private final OkHttpClientProperties okHttpclientProperties;

    private final OkHttpConnectionPoolSetMetrics okHttpConnectionPoolSetMetrics;
    private final Optional<OkHttpCacheSetMetrics> okHttpCacheSetMetrics;

    public RestOperationsFactory(final RestTemplateBuilder restTemplateBuilder,
                                 final OkHttpClientProperties okHttpclientProperties,
                                 final MeterRegistry meterRegistry, final MetricsProperties metricsProperties) {
        this.restTemplateBuilder = requireNonNull(restTemplateBuilder);
        this.okHttpclientProperties = requireNonNull(okHttpclientProperties);
        this.okHttpConnectionPoolSetMetrics = new OkHttpConnectionPoolSetMetrics("okhttp.pool", toTags(metricsProperties.getTags())).bind(meterRegistry);
        if (okHttpclientProperties.getCache().isEnabled()) {
            this.okHttpCacheSetMetrics = Optional.of(new OkHttpCacheSetMetrics("okhttp.cache", toTags(metricsProperties.getTags())).bind(meterRegistry));
        } else {
            this.okHttpCacheSetMetrics = Optional.empty();
        }
    }

    public CloseableRestOperations createRestOperations() {
        final var restTemplate = restTemplateBuilder.detectRequestFactory(false).build();
        final var closeables = new LinkedList<AutoCloseable>();
        if (okHttpclientProperties.isEnabled()) {
            final var httpClient = okHttpClient();
            closeables.add(() -> {
                okHttpConnectionPoolSetMetrics.removeConnectionPool(httpClient.connectionPool());
                httpClient.connectionPool().evictAll();
            });
            closeables.add(() -> {
                if (httpClient.cache() != null) {
                    okHttpCacheSetMetrics.ifPresent(metrics -> metrics.removeCache(httpClient.cache()));
                    httpClient.cache().close();
                    deleteRecursively(httpClient.cache().directory());
                }
            });
            restTemplate.setRequestFactory(new OkHttp3ClientHttpRequestFactory(httpClient));
        } else {
            restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        }
        return new CloseableRestOperations(restTemplate, closeables);
    }

    private OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(okHttpclientProperties.getProtocols())
                .connectTimeout(okHttpclientProperties.getConnectTimeout())
                .readTimeout(okHttpclientProperties.getReadTimeout())
                .writeTimeout(okHttpclientProperties.getWriteTimeout())
                .callTimeout(okHttpclientProperties.getCallTimeout())
                .connectionPool(connectionPool())
                .cache(cache())
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }

    private ConnectionPool connectionPool() {
        final var connectionPool = new ConnectionPool(okHttpclientProperties.getPool().getMaxIdleConnections(), okHttpclientProperties.getPool().getKeepAliveDuration().toMillis(), MILLISECONDS);
        okHttpConnectionPoolSetMetrics.addConnectionPool(connectionPool);
        return connectionPool;
    }

    private Cache cache() {
        if (okHttpclientProperties.getCache().isEnabled()) {
            final var cache = new Cache(new File(okHttpclientProperties.getCache().getBaseDirectory(), UUID.randomUUID().toString()), okHttpclientProperties.getCache().getMaxSize().toBytes());
            okHttpCacheSetMetrics.ifPresent(metrics -> metrics.addCache(cache));
            return cache;
        } else {
            return null;
        }
    }
}
