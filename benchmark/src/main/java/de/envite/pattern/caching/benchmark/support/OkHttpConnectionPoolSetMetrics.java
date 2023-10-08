package de.envite.pattern.caching.benchmark.support;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.ConnectionPool;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static java.util.Collections.newSetFromMap;
import static java.util.Objects.requireNonNull;

public class OkHttpConnectionPoolSetMetrics implements MeterBinder {

    public static final String DEFAULT_NAME_PREFIX = "okhttp.pool";

    private static final String TAG_STATE = "state";

    private final Set<ConnectionPool> connectionPools = newSetFromMap(new ConcurrentHashMap<>());

    private final String namePrefix;

    private final Iterable<Tag> tags;

    private final ThreadLocal<ConnectionPoolConnectionStats> connectionStats = new ThreadLocal<>();

    public OkHttpConnectionPoolSetMetrics(final Iterable<Tag> tags) {
        this(DEFAULT_NAME_PREFIX, tags);
    }

    public OkHttpConnectionPoolSetMetrics(final String namePrefix, final Iterable<Tag> tags) {
        this.namePrefix = requireNonNull(namePrefix);
        this.tags = requireNonNull(tags);
    }

    public void addConnectionPool(final ConnectionPool connectionPool) {
        if (connectionPool != null) {
            connectionPools.add(connectionPool);
        }
    }

    public void removeConnectionPool(final ConnectionPool connectionPool) {
        if (connectionPool != null) {
            connectionPools.remove(connectionPool);
        }
    }

    public OkHttpConnectionPoolSetMetrics bind(@NonNull final MeterRegistry registry) {
        bindTo(registry);
        return this;
    }

    @Override
    public void bindTo(@NonNull final MeterRegistry registry) {
        final var connectionCountName = namePrefix + ".connection.count";
        Gauge.builder(connectionCountName, connectionStats, cs -> {
                    if (cs.get() == null) {
                        cs.set(new ConnectionPoolConnectionStats());
                    }
                    return cs.get().getActiveCount();
                })
                .baseUnit(BaseUnits.CONNECTIONS)
                .description("The state of connections in the OkHttp connection pool")
                .tags(Tags.of(tags).and(TAG_STATE, "active"))
                .register(registry);
        Gauge.builder(connectionCountName, connectionStats, cs -> {
                    if (cs.get() == null) {
                        cs.set(new ConnectionPoolConnectionStats());
                    }
                    return cs.get().getIdleConnectionCount();
                })
                .baseUnit(BaseUnits.CONNECTIONS)
                .description("The state of connections in the OkHttp connection pool")
                .tags(Tags.of(tags).and(TAG_STATE, "idle"))
                .register(registry);
    }

    /**
     * Allow us to coordinate between active and idle, making sure they always sum to the
     * total available connections. Since we're calculating active from total-idle, we
     * want to synchronize on idle to make sure the sum is accurate.
     */
    private final class ConnectionPoolConnectionStats {

        private CountDownLatch uses = new CountDownLatch(0);

        private int idle;

        private int total;

        public int getActiveCount() {
            snapshotStatsIfNecessary();
            uses.countDown();
            return total - idle;
        }

        public int getIdleConnectionCount() {
            snapshotStatsIfNecessary();
            uses.countDown();
            return idle;
        }

        private void snapshotStatsIfNecessary() {
            if (uses.getCount() == 0) {
                idle = connectionPools.stream().mapToInt(ConnectionPool::idleConnectionCount).sum();
                total = connectionPools.stream().mapToInt(ConnectionPool::connectionCount).sum();
                uses = new CountDownLatch(2);
            }
        }

    }
}
