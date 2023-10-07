package de.envite.pattern.caching.benchmark.config;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapterFactory;
import de.envite.pattern.caching.benchmark.adapter.RestOperationsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
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
    public RestOperationsFactory restOperationsFactory(final OkHttpClientProperties okHttpclientProperties,
                                                       final HttpMessageConverters messageConverters) {
        return new RestOperationsFactory(okHttpclientProperties, messageConverters);
    }

}
