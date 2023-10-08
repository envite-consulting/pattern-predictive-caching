package de.envite.pattern.caching.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("web-resource")
public class WebResourceProperties {

    private News news = new News();

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public static class News {

        private CacheControl cacheControl = new CacheControl();

        public CacheControl getCacheControl() {
            return cacheControl;
        }

        public void setCacheControl(CacheControl cacheControl) {
            this.cacheControl = cacheControl;
        }

        public static class CacheControl {

            private boolean enabled = true;

            private Duration recommendedNews = Duration.ofMinutes(1);
            private Duration latestNews = Duration.ofMinutes(1);

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getRecommendedNews() {
                return recommendedNews;
            }

            public void setRecommendedNews(Duration recommendedNews) {
                this.recommendedNews = recommendedNews;
            }

            public Duration getLatestNews() {
                return latestNews;
            }

            public void setLatestNews(Duration latestNews) {
                this.latestNews = latestNews;
            }
        }

    }

}
