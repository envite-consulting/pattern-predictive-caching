package de.envite.pattern.caching.benchmark.support;

public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();

}
