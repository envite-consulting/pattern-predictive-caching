package de.envite.pattern.caching.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("web-resource")
public class WebResourceProperties {

    private Feed feed = new Feed();
    private Interests interests = new Interests();
    private Usernames usernames = new Usernames();

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Interests getInterests() {
        return interests;
    }

    public void setInterests(Interests interests) {
        this.interests = interests;
    }

    public Usernames getUsernames() {
        return usernames;
    }

    public void setUsernames(Usernames usernames) {
        this.usernames = usernames;
    }

    public static class Feed {

        private CacheControl cacheControl = new CacheControl();

        public CacheControl getCacheControl() {
            return cacheControl;
        }

        public void setCacheControl(CacheControl cacheControl) {
            this.cacheControl = cacheControl;
        }

        public static class CacheControl {

            private boolean enabled = true;

            private Duration byUser = Duration.ofMinutes(1);

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getByUser() {
                return byUser;
            }

            public void setByUser(Duration byUser) {
                this.byUser = byUser;
            }
        }
    }

    public static class Interests {

        private CacheControl cacheControl = new CacheControl();

        public CacheControl getCacheControl() {
            return cacheControl;
        }

        public void setCacheControl(CacheControl cacheControl) {
            this.cacheControl = cacheControl;
        }

        public static class CacheControl {

            private boolean enabled = true;

            private Duration all = Duration.ofMinutes(1);
            private Duration byUser = Duration.ofMinutes(1);

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getAll() {
                return all;
            }

            public void setAll(Duration all) {
                this.all = all;
            }

            public Duration getByUser() {
                return byUser;
            }

            public void setByUser(Duration byUser) {
                this.byUser = byUser;
            }
        }
    }

    public static class Usernames {

        private CacheControl cacheControl = new CacheControl();

        public CacheControl getCacheControl() {
            return cacheControl;
        }

        public void setCacheControl(CacheControl cacheControl) {
            this.cacheControl = cacheControl;
        }

        public static class CacheControl {

            private boolean enabled = true;

            private Duration all = Duration.ofMinutes(1);

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getAll() {
                return all;
            }

            public void setAll(Duration all) {
                this.all = all;
            }
        }
    }

}
