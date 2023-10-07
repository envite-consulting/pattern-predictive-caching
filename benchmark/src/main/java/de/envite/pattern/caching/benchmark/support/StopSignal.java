package de.envite.pattern.caching.benchmark.support;

import java.time.Duration;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BooleanSupplier;

import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StopSignal implements BooleanSupplier {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean stop = false;
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    private final Set<StopSignal> observers = newSetFromMap(new WeakHashMap<>());

    public void stop() {
        lock.writeLock().lock();
        try {
            stop = true;
            stopLatch.countDown();
            observers.forEach(StopSignal::stop);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean getAsBoolean() {
        return stop;
    }

    public boolean await(final Duration timeout) throws InterruptedException {
        return stopLatch.await(timeout.toMillis(), MILLISECONDS);
    }

    public StopSignal adapt(final StopSignal... stopSignals) {
        for (final var stopSignal : stopSignals) {
            try (final var lock = stopSignal.readLock()) {
                if (stopSignal.getAsBoolean()) {
                    stop();
                } else {
                    stopSignal.observers.add(this);
                }
            }
        }
        return this;
    }

    public AutoUnlockable readLock() {
        lock.readLock().lock();
        return new AutoUnlockable();
    }

    public class AutoUnlockable implements AutoCloseable {
        @Override
        public void close() {
            lock.readLock().unlock();
        }
    }

}
