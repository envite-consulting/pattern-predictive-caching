package de.envite.pattern.caching.benchmark.support;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

public class Timer {

    private final  long deadlineMs;
    private final LongSupplier currentTimeMsSupplier;

    private final BooleanSupplier remainingSupplier;
    private final BooleanSupplier expiredSupplier;

    public Timer(final Duration timeout, final LongSupplier currentTimeMsSupplier) {
        this(timeout.toMillis() + currentTimeMsSupplier.getAsLong(), currentTimeMsSupplier);
    }

    public Timer(final long deadlineMs, final LongSupplier currentTimeMsSupplier) {
        this.deadlineMs = deadlineMs;
        this.currentTimeMsSupplier = requireNonNull(currentTimeMsSupplier);
        this.remainingSupplier = this::isRemaining;
        this.expiredSupplier = this::isExpired;
    }

    public long deadlineMs() {
        return deadlineMs;
    }

    public boolean isRemaining() {
        return !isExpired();
    }

    public BooleanSupplier remaining() {
        return remainingSupplier;
    }

    public boolean isExpired()  {
        return this.currentTimeMsSupplier.getAsLong() >= this.deadlineMs;
    }

    public BooleanSupplier expired() {
        return expiredSupplier;
    }

}
