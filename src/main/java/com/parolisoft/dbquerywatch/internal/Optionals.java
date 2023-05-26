package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass class Optionals {

    // Optional.or() was implemented on JDK 9.
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> or(Optional<T> optional1, Supplier<Optional<T>> optional2Supplier) {
        if (optional1.isPresent()) {
            return optional1;
        } else {
            return optional2Supplier.get();
        }
    }
}
