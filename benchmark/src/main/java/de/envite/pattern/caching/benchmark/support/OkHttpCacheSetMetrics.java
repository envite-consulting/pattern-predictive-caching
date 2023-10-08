package de.envite.pattern.caching.benchmark.support;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.Cache;
import org.springframework.util.function.ThrowingFunction;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToLongFunction;

import static java.util.Collections.newSetFromMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class OkHttpCacheSetMetrics implements MeterBinder {

    public static final String DEFAULT_NAME_PREFIX = "okhttp.cache";

    private final Set<Cache> caches = newSetFromMap(new ConcurrentHashMap<>());
    private final String namePrefix;
    private final Iterable<Tag> tags;

    public OkHttpCacheSetMetrics(final Iterable<Tag> tags) {
        this(DEFAULT_NAME_PREFIX, tags);
    }

    public OkHttpCacheSetMetrics(final String namePrefix, final Iterable<Tag> tags) {
        this.namePrefix = namePrefix;
        this.tags = tags;
    }

    public void addCache(final Cache cache) {
        if (cache != null) {
            caches.add(cache);
        }
    }

    public void removeCache(final Cache cache) {
        if (cache != null) {
            caches.remove(cache);
        }
    }

    public OkHttpCacheSetMetrics bind(@NonNull final MeterRegistry registry) {
        bindTo(registry);
        return this;
    }

    @Override
    public void bindTo(@NonNull final MeterRegistry registry) {
        final var cacheHitsName = namePrefix + ".hits";
        FunctionCounter.builder(cacheHitsName, caches, value -> value.stream().mapToInt(Cache::hitCount).sum())
                .tags(tags)
                .register(registry);
        final var cacheMissesName = namePrefix + ".misses";
        FunctionCounter.builder(cacheMissesName, caches, value -> value.stream().mapToInt(Cache::networkCount).sum())
                .tags(tags)
                .register(registry);
        final var cacheMaxSizeName = namePrefix + ".size.max";
        Gauge.builder(cacheMaxSizeName, caches, value -> value.stream().mapToLong(Cache::maxSize).sum())
                .baseUnit(BaseUnits.BYTES)
                .tags(tags)
                .register(registry);
        final var cacheCurrentSizeName = namePrefix + ".size.current";
        Gauge.builder(cacheCurrentSizeName, caches, value -> value.stream().mapToLong(doQuietly(Cache::size, 0)).sum())
                .baseUnit(BaseUnits.BYTES)
                .tags(tags)
                .register(registry);
    }

    private <T> ToLongFunction<T> doQuietly(final ThrowingFunction<T,Long> supplier, final long defaultValue) {
        return obj -> {
            Optional<Long> result;
            try {
                result = ofNullable(supplier.applyWithException(obj));
            } catch (final Exception e) {
                result = empty();
            }
            return result.orElse(defaultValue);
        };
    }

}
