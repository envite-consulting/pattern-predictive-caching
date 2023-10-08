package de.envite.pattern.caching.feed.support;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.Cache;
import org.springframework.util.function.ThrowingFunction;

import java.util.Optional;
import java.util.function.ToDoubleFunction;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class OkHttpCacheMetrics implements MeterBinder {

    public static final String DEFAULT_NAME_PREFIX = "okhttp.cache";

    private final Cache cache;
    private final String namePrefix;
    private final Iterable<Tag> tags;

    public OkHttpCacheMetrics(final Cache cache, final Iterable<Tag> tags) {
        this(cache, DEFAULT_NAME_PREFIX, tags);
    }

    public OkHttpCacheMetrics(final Cache cache, final String namePrefix, final Iterable<Tag> tags) {
        this.cache = requireNonNull(cache);
        this.namePrefix = namePrefix;
        this.tags = tags;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        final var cacheHitsName = namePrefix + ".hits";
        FunctionCounter.builder(cacheHitsName, cache, Cache::hitCount)
                .tags(tags)
                .register(registry);
        final var cacheMissesName = namePrefix + ".misses";
        FunctionCounter.builder(cacheMissesName, cache, Cache::networkCount)
                .tags(tags)
                .register(registry);
        final var cacheMaxSizeName = namePrefix + ".size.max";
        Gauge.builder(cacheMaxSizeName, cache, Cache::maxSize)
                .baseUnit(BaseUnits.BYTES)
                .tags(tags)
                .register(registry);
        final var cacheCurrentSizeName = namePrefix + ".size.current";
        Gauge.builder(cacheCurrentSizeName, cache, doQuietly(Cache::size, 0))
                .baseUnit(BaseUnits.BYTES)
                .tags(tags)
                .register(registry);
    }

    private <I,O extends Number> ToDoubleFunction<I> doQuietly(final ThrowingFunction<I,O> supplier, final O defaultValue) {
        return obj -> {
            Optional<O> result;
            try {
                result = ofNullable(supplier.applyWithException(obj));
            } catch (final Exception e) {
                result = empty();
            }
            return result.orElse(defaultValue).doubleValue();
        };
    }

}
