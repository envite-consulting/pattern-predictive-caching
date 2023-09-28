package de.envite.pattern.caching.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
@ConfigurationProperties("feed")
public class FeedProperties {

    private int limit = 20;
    private Period period = Period.ofWeeks(1);

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}
