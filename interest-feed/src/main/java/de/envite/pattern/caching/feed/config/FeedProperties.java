package de.envite.pattern.caching.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Period;

@Configuration
@ConfigurationProperties("feed")
public class FeedProperties {

    private int limit = 20;
    private Period period = Period.ofWeeks(1);

    private boolean useRecommendedNews = true;
    private boolean useLatestNews = true;

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

    public boolean isUseRecommendedNews() {
        return useRecommendedNews;
    }

    public void setUseRecommendedNews(boolean useRecommendedNews) {
        this.useRecommendedNews = useRecommendedNews;
    }

    public boolean isUseLatestNews() {
        return useLatestNews;
    }

    public void setUseLatestNews(boolean useLatestNews) {
        this.useLatestNews = useLatestNews;
    }
}
