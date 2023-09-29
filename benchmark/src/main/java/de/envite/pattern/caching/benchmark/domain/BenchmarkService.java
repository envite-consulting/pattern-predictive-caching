package de.envite.pattern.caching.benchmark.domain;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapter;
import de.envite.pattern.caching.benchmark.config.BenchmarkProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BenchmarkService {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);
    private final BenchmarkProperties benchmarkProperties;
    private final FeedAdapter feedAdapter;
    private final ExecutorService executorService;
    private final AtomicBoolean stop;

    public BenchmarkService(@Autowired BenchmarkProperties benchmarkProperties,
                            @Autowired FeedAdapter feedAdapter) {
        this.benchmarkProperties = benchmarkProperties;
        this.feedAdapter = feedAdapter;
        this.executorService = Executors.newCachedThreadPool();
        stop = new AtomicBoolean(false);
    }

    public void runBenchmark() throws InterruptedException {
        log.info("Waiting for {} seconds until all applications are ready...", benchmarkProperties.getInitialWaitingPeriod().toSeconds());
        Thread.sleep(benchmarkProperties.getInitialWaitingPeriod().toMillis());

        List<String> usernames = feedAdapter.getAllUsernames(benchmarkProperties.getNumberOfUsers());
        log.info("Retrieved {} usernames for processing.", usernames.size());

        log.info("Application is ready. Waiting for {} seconds until start of benchmark...", benchmarkProperties.getInitialWaitingPeriod().toSeconds());
        Thread.sleep(benchmarkProperties.getInitialWaitingPeriod().toMillis());
        log.info("Starting Benchmark after {} seconds initial waiting time...", benchmarkProperties.getInitialWaitingPeriod());

        CompletableFuture<?>[] completableFutures = usernames.stream()
                .map((username) -> new UserSimulator(benchmarkProperties, feedAdapter, username, stop))
                .map((userSimulator) -> CompletableFuture.runAsync(userSimulator, executorService))
                .toArray(CompletableFuture[]::new);

        Thread.sleep(benchmarkProperties.getTestDuration().toMillis());
        stop.set(true);

        CompletableFuture.allOf(completableFutures).join();
        executorService.shutdownNow();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        log.info("Finished benchmark.");
    }

    @PreDestroy
    public void close() {
        stop.set(true);
        executorService.shutdownNow();
    }
}
