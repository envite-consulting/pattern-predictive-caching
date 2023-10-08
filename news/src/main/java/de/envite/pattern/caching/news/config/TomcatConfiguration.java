package de.envite.pattern.caching.news.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static de.envite.pattern.caching.news.support.MetricsSupport.toTags;
import static io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics.monitor;

@Configuration(proxyBeanMethods = false)
public class TomcatConfiguration {

    @Bean
    @ConditionalOnProperty(value = "server.tomcat.threads.executor", havingValue = "virtual")
    public TomcatProtocolHandlerCustomizer<?> protocolHandler(@Qualifier("tomcatProtocolHandlerConnectorExecutor") final ExecutorService protocolHandlerConnectorExecutor,
                                                              @Qualifier("tomcatProtocolHandlerUtilityExecutor") final ScheduledExecutorService protocolHandlerUtilityExecutor) {
        return protocolHandler -> {
            protocolHandler.setExecutor(protocolHandlerConnectorExecutor);
            protocolHandler.setUtilityExecutor(protocolHandlerUtilityExecutor);
        };
    }

    @Bean(name = "tomcatProtocolHandlerConnectorExecutor")
    @ConditionalOnProperty(value = "server.tomcat.threads.executor", havingValue = "virtual")
    public ExecutorService protocolHandlerConnectorExecutor(final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final var name = "tomcat-connector-virtual";
        final var executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(name + "-", 0).factory());
        return monitor(meterRegistry, executorService, name, toTags(properties.getTags()));
    }

    @Bean(name = "tomcatProtocolHandlerUtilityExecutor")
    @ConditionalOnProperty(value = "server.tomcat.threads.executor", havingValue = "virtual")
    public ScheduledExecutorService protocolHandlerUtilityExecutor(final MeterRegistry meterRegistry, final MetricsProperties properties) {
        final var name = "tomcat-utility-virtual";
        final var threadFactory = Thread.ofVirtual().name(name + "-", 0).factory();
        final var scheduledExecutorService = Executors.newScheduledThreadPool(1, threadFactory);
        return monitor(meterRegistry, scheduledExecutorService, name, toTags(properties.getTags()));
    }

}
