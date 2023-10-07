package de.envite.pattern.caching.benchmark.domain;

public class BenchmarkException extends RuntimeException {

    public BenchmarkException(final String message) {
        super(message);
    }

    public BenchmarkException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
