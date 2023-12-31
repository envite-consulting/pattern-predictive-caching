package de.envite.pattern.caching.feed.config;

import de.envite.pattern.caching.feed.support.OkHttpCacheMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static de.envite.pattern.caching.feed.support.MetricsSupport.toTags;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration(proxyBeanMethods = false)
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .requestFactory(SimpleClientHttpRequestFactory::new) // request factory which is also  used by default if no client library is in classpath
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "okhttp.client.enabled", havingValue = "true")
    public RestTemplateCustomizer httpRequestFactoryRestTemplateCustomizer(final ClientHttpRequestFactory httpRequestFactory) {
        return restTemplate -> restTemplate.setRequestFactory(httpRequestFactory);
    }

    @Bean @Lazy
    public ClientHttpRequestFactory httpRequestFactory(final OkHttpClient httpClient) {
        return new OkHttp3ClientHttpRequestFactory(httpClient);
    }

    @Bean @Lazy
    public OkHttpClient httpClient(@Autowired final OkHttpClientProperties clientProperties,
                                   @Autowired final ConnectionPool connectionPool, @Autowired(required = false) final Cache cache) {
        return new OkHttpClient.Builder()
                .protocols(clientProperties.getProtocols())
                .connectTimeout(clientProperties.getConnectTimeout())
                .readTimeout(clientProperties.getReadTimeout())
                .writeTimeout(clientProperties.getWriteTimeout())
                .callTimeout(clientProperties.getCallTimeout())
                .connectionPool(connectionPool)
                .cache(cache)
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean(destroyMethod = "evictAll") @Lazy
    public ConnectionPool connectionPool(final OkHttpClientProperties clientProperties, final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final var connectionPool = new ConnectionPool(clientProperties.getPool().getMaxIdleConnections(), clientProperties.getPool().getKeepAliveDuration().toMillis(), MILLISECONDS);
        new OkHttpConnectionPoolMetrics(connectionPool, "okhttp.pool", toTags(properties.getTags())).bindTo(meterRegistry);
        return connectionPool;
    }

    @Bean @Lazy
    @ConditionalOnProperty(name = "okhttp.client.cache.enabled", havingValue = "true")
    public Cache cache(final OkHttpClientProperties clientProperties, final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final var cache = new Cache(clientProperties.getCache().getDirectory(), clientProperties.getCache().getMaxSize().toBytes());
        new OkHttpCacheMetrics(cache, "okhttp.cache", toTags(properties.getTags())).bindTo(meterRegistry);
        return cache;
    }
}
