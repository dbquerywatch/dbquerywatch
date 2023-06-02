package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@UtilityClass
class Optionals {

    @SafeVarargs
    public static <T> Optional<T> or(Supplier<Optional<T>>... suppliers) {
        return Stream.of(suppliers)
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
