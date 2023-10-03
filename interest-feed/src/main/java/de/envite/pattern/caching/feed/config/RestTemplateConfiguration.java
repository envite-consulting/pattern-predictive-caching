package de.envite.pattern.caching.feed.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static de.envite.pattern.caching.feed.support.MetricsSupport.toTags;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration(proxyBeanMethods = false)
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    @ConditionalOnProperty(value = "okhttp.client.enabled", havingValue = "true")
    public RestTemplateCustomizer httpRequestFactoryRestTemplateCustomizer(final ClientHttpRequestFactory httpRequestFactory) {
        return restTemplate -> restTemplate.setRequestFactory(httpRequestFactory);
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(final OkHttpClient httpClient) {
        return new OkHttp3ClientHttpRequestFactory(httpClient);
    }

    @Bean
    public OkHttpClient httpClient(final OkHttpClientProperties clientProperties, final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final var connectionPool = new ConnectionPool(clientProperties.getPool().getMaxIdleConnections(), clientProperties.getPool().getKeepAliveDuration().toMillis(), MILLISECONDS);
        new OkHttpConnectionPoolMetrics(connectionPool, "okhttp.pool", toTags(properties.getTags())).bindTo(meterRegistry);
        return new OkHttpClient.Builder()
                .protocols(clientProperties.getProtocols())
                .connectTimeout(clientProperties.getConnectTimeout())
                .readTimeout(clientProperties.getReadTimeout())
                .writeTimeout(clientProperties.getWriteTimeout())
                .callTimeout(clientProperties.getCallTimeout())
                .connectionPool(connectionPool)
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .build();
    }

}
