package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.Optional;

@UtilityClass
public class TestMethodTracker {

    private static final ThreadLocal<TestMethod> CURRENT_TEST_METHOD = new ThreadLocal<>();

    public static void setCurrentTestMethod(Class<?> testClass, Method testMethod) {
        CURRENT_TEST_METHOD.set(TestMethod.of(testClass.getCanonicalName(), testMethod.getName()));
    }

    public static void unsetCurrentTestMethod() {
        CURRENT_TEST_METHOD.remove();
    }

    public static Optional<TestMethod> getCurrentTestMethod() {
        return Optional.ofNullable(CURRENT_TEST_METHOD.get());
    }
}
