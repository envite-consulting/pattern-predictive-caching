package de.envite.pattern.caching.benchmark.domain;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapter;
import de.envite.pattern.caching.benchmark.adapter.FeedAdapterFactory;
import de.envite.pattern.caching.benchmark.config.BenchmarkProperties;
import de.envite.pattern.caching.benchmark.support.OrExpression;
import de.envite.pattern.caching.benchmark.support.StopSignal;
import de.envite.pattern.caching.benchmark.support.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.context.LifecycleProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static de.envite.pattern.caching.benchmark.support.MetricsSupport.toTags;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class BenchmarkService implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);

    private final BenchmarkProperties benchmarkProperties;
    private final LifecycleProperties lifecycleProperties;
    private final FeedAdapterFactory feedAdapterFactory;
    private final ExecutorService executorService;

    private final StopSignal destroySignal = new StopSignal();

    private final AtomicInteger currentUserSimulationCount = new AtomicInteger(0);

    @Autowired
    public BenchmarkService(final BenchmarkProperties benchmarkProperties,
                            final LifecycleProperties lifecycleProperties,
                            final FeedAdapterFactory feedAdapterFactory,
                            final MeterRegistry meterRegistry, final MetricsProperties metricsProperties) {
        this(benchmarkProperties, lifecycleProperties, feedAdapterFactory,
                Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("benchmark-", 0).factory()),
                meterRegistry, metricsProperties);
    }

    BenchmarkService(final BenchmarkProperties benchmarkProperties,
                     final LifecycleProperties lifecycleProperties,
                     final FeedAdapterFactory feedAdapterFactory,
                     final ExecutorService userSimulatorExecutor,
                     final MeterRegistry meterRegistry, final MetricsProperties metricsProperties) {
        this.benchmarkProperties = benchmarkProperties;
        this.lifecycleProperties = lifecycleProperties;
        this.feedAdapterFactory = feedAdapterFactory;
        this.executorService = userSimulatorExecutor;

        Gauge.builder("benchmark.user.simulations.current", currentUserSimulationCount, AtomicInteger::get)
                .tags(toTags(metricsProperties.getTags()))
                .register(meterRegistry);
    }

    public void runBenchmark(final StopSignal stopSignal) throws BenchmarkException, InterruptedException {
        final var internalStopSignal = new StopSignal().adapt(stopSignal);
        final var benchmarkExecution = runBenchmarkAsync(internalStopSignal);
        try {
            awaitCompletion(benchmarkExecution);
        }  catch (final InterruptedException e) {
            internalStopSignal.stop();
            final var ex = doQuietly(() -> awaitCompletion(benchmarkExecution));
            Thread.currentThread().interrupt();
            throw new InterruptedException(ex.isPresent() ? ex.get().getMessage() : "Benchmark has been interrupted.");
        }
    }

    public CompletableFuture<Void> runBenchmarkAsync(final StopSignal stopSignal) {
        try (final var destroyLock = destroySignal.readLock(); final var stopLock = stopSignal.readLock()) {
            if (!destroySignal.getAsBoolean() && !stopSignal.getAsBoolean()) {
                return completedFuture(stopSignal).thenComposeAsync(this::runBenchmarkAsyncInternal, executorService);
            }
            return failedFuture(new BenchmarkException("Benchmark has been aborted."));
        }
    }

    private CompletableFuture<Void> runBenchmarkAsyncInternal(final StopSignal stopSignal) throws BenchmarkException {
        sleep(benchmarkProperties.getInitialWaitingPeriod(), stopSignal, duration -> log.info("Waiting {} seconds until all applications are ready ...", duration.toSeconds()));
        final var usernames = fetchUsernames(benchmarkProperties.getNumberOfUsers());
        sleep(benchmarkProperties.getInitialWaitingPeriod(), stopSignal, duration -> log.info("Benchmark is ready. Waiting for {} seconds until start of benchmark ...", duration.toSeconds()));
        return runBenchmarkAsync(usernames, stopSignal);
    }

    private List<String> fetchUsernames(final int numberOfUsers) throws BenchmarkException {
        log.info("Requesting {} usernames ...", benchmarkProperties.getNumberOfUsers());
        try (final FeedAdapter feedAdapter = feedAdapterFactory.createFeedAdapter()) {
            final var usernames = feedAdapter.getAllUsernames(numberOfUsers);
            log.info("Retrieved {} usernames.", usernames.size());
            return usernames;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BenchmarkException("Requesting usernames has been interrupted.", e);
        } catch (final RuntimeException e) {
            throw new BenchmarkException(String.format("Requesting usernames failed: %s", e.getMessage()), e);
        }
    }

    public CompletableFuture<Void> runBenchmarkAsync(final List<String> usernames, final StopSignal stopSignal) {
        log.info("Starting Benchmark ...");
        final var startTimeMillis = System.currentTimeMillis();
        final var timer = new Timer(benchmarkProperties.getTestDuration(), System::currentTimeMillis);
        final var exceptionSignal = new StopSignal();
        final var stopCondition = new OrExpression(timer.expired(), stopSignal, exceptionSignal, destroySignal);
        final var userSimulations = new ArrayList<CompletableFuture<Void>>(usernames.size());
        try (final var destroyLock = destroySignal.readLock(); final var stopLock = stopSignal.readLock()) {
            if (!destroySignal.getAsBoolean() && !stopSignal.getAsBoolean()) {
                for (final var username : usernames) {
                    currentUserSimulationCount.incrementAndGet();
                    var userSimulator = new UserSimulator(feedAdapterFactory, username, benchmarkProperties::getDate, benchmarkProperties.getRequestWaitingPeriod(), stopCondition);
                    userSimulations.add(runAsync(userSimulator, executorService).whenComplete(stopOnException(exceptionSignal)).whenComplete((unused, throwable) -> currentUserSimulationCount.decrementAndGet()));
                }
            }
        }
        return allOf(userSimulations.toArray(CompletableFuture<?>[]::new))
                .handle((unused, throwable) -> {
                    final var durationMs = System.currentTimeMillis() - startTimeMillis;
                    if (throwable == null &&!stopSignal.getAsBoolean() && !destroySignal.getAsBoolean()) {
                        log.info("Finished benchmark successfully after {} ms ...", durationMs);
                        return null;
                    } else {
                        throw new BenchmarkException(String.format("Benchmark aborted after %d ms of %d ms ...", durationMs, benchmarkProperties.getTestDuration().toMillis()));
                    }
                });
    }

    @Override
    public void destroy() throws InterruptedException {
        log.info("Closing Benchmark service ...");
        final var startTimeMillis = System.currentTimeMillis();
        destroySignal.stop();
        executorService.shutdown();
        final var gracefulTimeoutMs = lifecycleProperties.getTimeoutPerShutdownPhase().toMillis() * 3 / 4;
        if (!executorService.awaitTermination(gracefulTimeoutMs, MILLISECONDS)) {
            log.warn("Could not gracefully stop running benchmarks within {} ms. Execution will be interrupted.", gracefulTimeoutMs);
            executorService.shutdownNow();
            final var forcefulTimeoutMs = lifecycleProperties.getTimeoutPerShutdownPhase().toMillis() - gracefulTimeoutMs;
            if (!executorService.awaitTermination(forcefulTimeoutMs, MILLISECONDS)) {
                log.warn("Could not forcefully stop running benchmarks within {} ms. Terminating ...", forcefulTimeoutMs);
            }
        }
        log.info("Benchmark service closed after {} ms.", (System.currentTimeMillis() - startTimeMillis));
    }

    private void awaitCompletion(final CompletableFuture<?> benchmarkExecution) throws BenchmarkException, InterruptedException {
        try {
            benchmarkExecution.get();
        } catch (final ExecutionException e) {
            throw new BenchmarkException(e.getCause().getMessage(), e.getCause());
        } catch (final CancellationException e) {
            throw new BenchmarkException("Benchmark has been aborted.", e);
        }
    }

    private Optional<Exception> doQuietly(final AutoCloseable runnable) {
        try {
            runnable.close();
            return Optional.empty();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.of(e);
        } catch (final Exception e) {
            return Optional.of(e);
        }
    }

    private void sleep(final Duration duration, final StopSignal stopSignal, final Consumer<Duration> logger) throws BenchmarkException {
        logger.accept(duration);
        try {
            if (new StopSignal().adapt(destroySignal, stopSignal).await(duration)) {
                throw new BenchmarkException("Benchmark has been aborted during sleep.");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BenchmarkException("Benchmark has been interrupted during sleep.");
        }
    }

    private <T> BiConsumer<? super T, ? super Throwable> stopOnException(final StopSignal stopSignal) {
        return (unused, throwable) -> ofNullable(throwable).ifPresent(t -> stopSignal.stop());
    }
}
