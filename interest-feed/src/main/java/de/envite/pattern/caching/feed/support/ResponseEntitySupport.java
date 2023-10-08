package de.envite.pattern.caching.feed.support;

import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class ResponseEntitySupport {

    private ResponseEntitySupport() {}

    @SafeVarargs
    public static <T> ResponseEntity<T> ok(final T response, final Entry<BooleanSupplier, Function<ResponseEntity.BodyBuilder, ResponseEntity.BodyBuilder>>... conditionalCustomizers) {
        return responseEntity(ResponseEntity.ok(), builder -> builder.body(response), conditionalCustomizers);
    }

    @SafeVarargs
    public static <T> ResponseEntity<T> responseEntity(final ResponseEntity.BodyBuilder builder,
                                                      final Function<ResponseEntity.BodyBuilder, ResponseEntity<T>> buildFunction,
                                                      final Entry<BooleanSupplier, Function<ResponseEntity.BodyBuilder, ResponseEntity.BodyBuilder>>... conditionalCustomizers) {
        Arrays.stream(conditionalCustomizers)
                .filter(e -> e.getKey().getAsBoolean())
                .map(Entry::getValue)
                .forEach(c -> c.apply(builder));
        return buildFunction.apply(builder);
    }
}
