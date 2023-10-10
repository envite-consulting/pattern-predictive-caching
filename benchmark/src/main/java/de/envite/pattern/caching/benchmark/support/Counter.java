package de.envite.pattern.caching.benchmark.support;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

public class Counter {

    private final long maxCount;

    private final AtomicLong currentCount = new AtomicLong(0);

    private final BooleanSupplier isRemainingSupplier;
    private final BooleanSupplier isReachedSupplier;

    public Counter(final long maxCount) {
        this.maxCount = maxCount;
        this.isRemainingSupplier = this::isRemaining;
        this.isReachedSupplier = this::isReached;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public long getCurrentCount() {
        return currentCount.get();
    }

    public long getRemainingCount() {
        return maxCount - currentCount.get();
    }

    public boolean isRemaining() {
        return !isReached();
    }

    public BooleanSupplier remaining() {
        return isRemainingSupplier;
    }

    public boolean isReached() {
        return currentCount.getAndIncrement() >= maxCount;
    }

    public BooleanSupplier reached() {
        return isReachedSupplier;
    }

}
