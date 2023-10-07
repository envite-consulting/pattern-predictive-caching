package de.envite.pattern.caching.benchmark.support;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class OrExpression implements BooleanSupplier {

    private final Collection<BooleanSupplier> suppliers;

    public OrExpression(final BooleanSupplier... suppliers) {
        this(List.of(suppliers));
    }

    public OrExpression(final Collection<BooleanSupplier> suppliers) {
        this.suppliers = Objects.requireNonNull(suppliers);
    }

    @Override
    public boolean getAsBoolean() {
        return suppliers.stream().anyMatch(BooleanSupplier::getAsBoolean);
    }
}
