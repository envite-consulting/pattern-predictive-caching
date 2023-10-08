package de.envite.pattern.caching.benchmark.domain;

import de.envite.pattern.caching.benchmark.adapter.FeedAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class UserSimulator implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UserSimulator.class);

    private final FeedAdapterFactory feedAdapterFactory;
    private final String username;
    private final Supplier<LocalDate> dateSupplier;
    private final Duration requestWaitingPeriod;
    private final BooleanSupplier stopCondition;

    public UserSimulator(final FeedAdapterFactory feedAdapterFactory,
                         final String username, final Supplier<LocalDate> dateSupplier, final Duration requestWaitingPeriod,
                         final BooleanSupplier stopCondition) {
        this.feedAdapterFactory = requireNonNull(feedAdapterFactory);
        this.username = requireNonNull(username);
        this.dateSupplier = requireNonNull(dateSupplier);
        this.requestWaitingPeriod = requireNonNull(requestWaitingPeriod);
        this.stopCondition = requireNonNull(stopCondition);
    }

    @Override
    public void run() {
        final var feedAdapter = feedAdapterFactory.createFeedAdapter();
        while(!stopCondition.getAsBoolean() && !Thread.currentThread().isInterrupted()) {
            try {
                feedAdapter.getFeedByUser(username, dateSupplier.get());
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (final RuntimeException e) {
                log.warn(String.format("Requesting feed for user '%s' failed: %s", username, e.getMessage()), e);
            }
            if (requestWaitingPeriod.isPositive()) {
                try {
                    Thread.sleep(requestWaitingPeriod.toMillis());
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        feedAdapter.close();
        if (Thread.currentThread().isInterrupted()) {
            throw new BenchmarkException("User simulation has been interrupted.");
        }
    }
}
