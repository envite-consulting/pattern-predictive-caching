package de.envite.pattern.caching.feed.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.stream.Collectors.toSet;

@Configuration(proxyBeanMethods = false)
public class TomcatConfiguration {

    @Bean
    @ConditionalOnProperty(value = "thread.virtual", havingValue = "true")
    public TomcatProtocolHandlerCustomizer<?> protocolHandler(final ExecutorService protocolHandlerConnectorExecutor, ScheduledExecutorService protocolHandlerUtilityExecutor) {
        return protocolHandler -> {
            protocolHandler.setExecutor(protocolHandlerConnectorExecutor);
            protocolHandler.setUtilityExecutor(protocolHandlerUtilityExecutor);
        };
    }

    @Bean
    @ConditionalOnProperty(value = "thread.virtual", havingValue = "true")
    public ExecutorService protocolHandlerConnectorExecutor(final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final String name = "tomcat-connector-virt";
        final ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(name + "-", 0).factory());
        return ExecutorServiceMetrics.monitor(meterRegistry, executor, name, toTags(properties.getTags()));
    }

    @Bean
    @ConditionalOnProperty(value = "thread.virtual", havingValue = "true")
    public ScheduledExecutorService protocolHandlerUtilityExecutor(final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final String name = "tomcat-utility-virt";
        final ThreadFactory threadFactory = Thread.ofVirtual().name(name + "-", 0).factory();
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, threadFactory);
        return ExecutorServiceMetrics.monitor(meterRegistry, executor, name, toTags(properties.getTags()));
    }

    private static Tags toTags(final Map<String, String> tags) {
        return Tags.of(tags.entrySet().stream()
                .map(e -> Tag.of(e.getKey(), e.getValue()))
                .collect(toSet()));
    }
}
