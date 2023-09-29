package de.envite.pattern.caching.benchmark.domain;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapter;
import de.envite.pattern.caching.benchmark.config.BenchmarkProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class UserSimulator implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UserSimulator.class);
    private final BenchmarkProperties benchmarkProperties;
    private final FeedAdapter feedAdapter;
    private final String username;
    private final AtomicBoolean stop;

    public UserSimulator(BenchmarkProperties benchmarkProperties,
                         FeedAdapter feedAdapter,
                         String username,
                         AtomicBoolean stop) {
        this.benchmarkProperties = benchmarkProperties;
        this.feedAdapter = feedAdapter;
        this.username = username;
        this.stop = stop;
    }

    @Override
    public void run() {
        while(!stop.get()) {
            try {
                feedAdapter.getFeedByUser(username, benchmarkProperties.getDate());
            }catch (Exception e) {
                log.warn(String.format("Benchmark is cancelled because error occurred at retrieving feed for user %s because of exception: %s", username, e.getMessage()), e);
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(benchmarkProperties.getRequestWaitingPeriod().toMillis());
            } catch (InterruptedException e) {
                log.warn(String.format("Benchmark is cancelled for user %s because of thread was interrupted: %s", username, e.getMessage()), e);
                throw new RuntimeException(e);
            }
        }
    }
}
