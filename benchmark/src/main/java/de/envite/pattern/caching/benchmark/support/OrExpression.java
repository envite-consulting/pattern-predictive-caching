package de.envite.pattern.caching.benchmark.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class OrExpression implements BooleanSupplier {

    private final Collection<BooleanSupplier> suppliers;

    public OrExpression(final BooleanSupplier... suppliers) {
        this(List.of(suppliers));
    }

    public OrExpression(final Collection<BooleanSupplier> suppliers) {
        this.suppliers = requireNonNull(suppliers);
    }

    public OrExpression with(final boolean condition, Supplier<BooleanSupplier> factory) {
        if (condition) {
            return with(factory.get());
        }
        return this;
    }

    public OrExpression with(final BooleanSupplier supplier) {
        final var newSuppliers = new ArrayList<>(suppliers);
        newSuppliers.add(supplier);
        return new OrExpression(newSuppliers);
    }

    @Override
    public boolean getAsBoolean() {
        return suppliers.stream().anyMatch(BooleanSupplier::getAsBoolean);
    }
}
