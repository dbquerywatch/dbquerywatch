package org.dbquerywatch.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringsTest {

    @ParameterizedTest
    @CsvSource(value = {
        "car.pet, car, false, '.', true",
        "car.pet, car, true,  '.', true",
        "car.pet, Car, false, '.', false",
        "car.pet, ca,  true, '.',  false",
        "car.pet, ca,  false, '.', false",
        "car, car.pet, true,  '.', false",
        "car, car,     false, '.', true",
        "car, Car,     true, '.',  true",
    })
    void prefixedBy_should_handle(
        String str,
        String prefix,
        boolean ignoreCase,
        char separator,
        boolean expectedResult
    ) {
        assertEquals(expectedResult, Strings.prefixedBy(str, prefix, ignoreCase, separator));
    }
}
