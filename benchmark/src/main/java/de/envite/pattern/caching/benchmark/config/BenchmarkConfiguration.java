package de.envite.pattern.caching.benchmark.config;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapterFactory;
import de.envite.pattern.caching.benchmark.adapter.RestOperationsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class BenchmarkConfiguration {

    @Bean
    public FeedAdapterFactory feedAdapterFactory(final RestOperationsFactory restOperationsFactory,
                                                 @Value("${service.interestFeed.url}") final String feedServiceUrl) {
        return new FeedAdapterFactory(restOperationsFactory, feedServiceUrl);
    }

    @Bean
    public RestOperationsFactory restOperationsFactory(final RestTemplateBuilder restTemplateBuilder,
                                                       final OkHttpClientProperties okHttpclientProperties,
                                                       final MeterRegistry meterRegistry, final MetricsProperties metricsProperties) {
        return new RestOperationsFactory(restTemplateBuilder, okHttpclientProperties, meterRegistry, metricsProperties);
    }

}
