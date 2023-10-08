package de.envite.pattern.caching.feed.config;

import okhttp3.Protocol;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Configuration
@ConfigurationProperties("okhttp.client")
public class OkHttpClientProperties {

    /**
     * Defines if the OkHttp client should be used.
     */
    private boolean enabled = false;

    /**
     * Configure the protocols used by this client to communicate with remote servers.
     *
     * The following protocols are currently supported:
     *
     * * [http/1.1][rfc_2616]
     * * [h2][rfc_7540]
     * * [h2 with prior knowledge(cleartext only)][rfc_7540_34]
     *
     * If multiple protocols are specified, [ALPN][alpn] will be used to negotiate a transport.
     * Protocol negotiation is only attempted for HTTPS URLs.
     */
    private List<Protocol> protocols = List.of(Protocol.HTTP_1_1);

    /**
     * Defines a time period in which our client should establish a connection with a target host.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);
    /**
     * Defines a maximum time of inactivity between two data packets when waiting for the server’s response.
     */
    private Duration readTimeout = Duration.ofSeconds(10);
    /**
     * Defines a maximum time of inactivity between two data packets when sending the request to the server.
     */
    private Duration writeTimeout = Duration.ofSeconds(10);

    /**
     * Defines a time limit for a complete HTTP call.
     * This includes resolving DNS, connecting, writing the request body, server processing, as well as reading the response body.
     */
    private Duration callTimeout = Duration.ZERO;

    /**
     * Manages reuse of HTTP and HTTP/2 connections for reduced network latency. HTTP requests that
     * share the same [Address] may share a [Connection]. This class implements the policy
     * of which connections to keep open for future use.
     */
    private Pool pool = new Pool();

    /**
     * Sets the response cache to be used to read and write cached responses.
     */
    private Cache cache = new Cache();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<Protocol> protocols) {
        this.protocols = protocols;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(Duration callTimeout) {
        this.callTimeout = callTimeout;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class Pool {

        /**
         * Defines the maximum number of idle connections id the pool.
         */
        private int maxIdleConnections = 5;
        /**
         * Defines the duration after which idle connections are evicted.
         */
        private Duration keepAliveDuration = Duration.ofMinutes(5);

        public int getMaxIdleConnections() {
            return maxIdleConnections;
        }

        public void setMaxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
        }

        public Duration getKeepAliveDuration() {
            return keepAliveDuration;
        }

        public void setKeepAliveDuration(Duration keepAliveDuration) {
            this.keepAliveDuration = keepAliveDuration;
        }
    }

    public static class Cache {

        /**
         * Defines if a cache should be used for OkHttp.
         */
        private boolean enabled = false;

        /**
         * Cache directory which is being exclusively owned by a single instance.
         */
        private File directory = new File(System.getProperty("java.io.tmpdir"), "http_cache-" + UUID.randomUUID());

        /**
         * The maximum size of the cache in bytes.
         */
        private DataSize maxSize = DataSize.ofMegabytes(10);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public File getDirectory() {
            return directory;
        }

        public void setDirectory(File directory) {
            this.directory = directory;
        }

        public DataSize getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(DataSize maxSize) {
            this.maxSize = maxSize;
        }
    }
}
