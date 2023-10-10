package de.envite.pattern.caching.benchmark.support;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Stopwatch implements Lifecycle {

    private final LongSupplier currentTimeMsSupplier;

    private final AtomicLong startTimeMs = new AtomicLong(0);
    private final AtomicLong endTimeMs = new AtomicLong(0);

    public Stopwatch(final LongSupplier currentTimeMsSupplier) {
        this.currentTimeMsSupplier = requireNonNull(currentTimeMsSupplier);
    }

    public static Stopwatch createAndAdd(final LongSupplier currentTimeMsSupplier, final Collection<Stopwatch> stopwatches) {
        final var stopwatch = new Stopwatch(currentTimeMsSupplier);
        stopwatches.add(stopwatch);
        return stopwatch;
    }

    @Override
    public void start() {
        if (!isStarted()) {
            startTimeMs.set(currentTimeMsSupplier.getAsLong());
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            endTimeMs.set(currentTimeMsSupplier.getAsLong());
        }
    }

    @Override
    public boolean isRunning() {
        return isStarted() && !isStopped();
    }

    public boolean isStarted() {
        return startTimeMs.get() > 0;
    }

    public boolean isStopped() {
        return endTimeMs.get() > 0;
    }

    public long elapsedTime(final TimeUnit timeUnit) {
        return timeUnit.convert(elapsedTimeMs(), MILLISECONDS);
    }

    public long elapsedTimeMs() {
        if (!isStarted()) {
            return 0;
        }
        if (isStopped()) {
            return endTimeMs.get() - startTimeMs.get();
        }
        return currentTimeMsSupplier.getAsLong() - startTimeMs.get();
    }
}
