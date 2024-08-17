package org.dbquerywatch.application.domain.service;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class TestMethodIdRepository {

    private static final AtomicInteger THREAD_LOCAL_HITS = new AtomicInteger();
    private static final AtomicInteger MDC_HITS = new AtomicInteger();

    private TestMethodIdRepository() {
    }

    static Optional<String> load() {
        return or(
            () -> meteredOptional(ThreadLocalTestMethodIdRepository.load(), THREAD_LOCAL_HITS),
            () -> meteredOptional(MdcTestMethodIdRepository.load(), MDC_HITS)
        );
    }

    public static void save(String uniqueId) {
        ThreadLocalTestMethodIdRepository.save(uniqueId);
    }

    public static void clear() {
        ThreadLocalTestMethodIdRepository.clear();
    }

    public static void resetMetrics() {
        THREAD_LOCAL_HITS.getAndSet(0);
        MDC_HITS.getAndSet(0);
    }

    public static int getThreadLocalHits() {
        return THREAD_LOCAL_HITS.get();
    }

    public static int getMdcHits() {
        return MDC_HITS.get();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> Optional<T> meteredOptional(Optional<T> optional, AtomicInteger hits) {
        if (optional.isPresent()) {
            hits.incrementAndGet();
        }
        return optional;
    }

    private static final class ThreadLocalTestMethodIdRepository {

        private static final ThreadLocal<String> TEST_METHOD_ID = new ThreadLocal<>();

        private ThreadLocalTestMethodIdRepository() {
        }

        private static Optional<String> load() {
            return Optional.ofNullable(TEST_METHOD_ID.get());
        }

        private static void save(String uniqueId) {
            TEST_METHOD_ID.set(TestMethodIdSupport.generateTestMethodId(uniqueId));
        }

        private static void clear() {
            TEST_METHOD_ID.remove();
        }
    }

    private static final class MdcTestMethodIdRepository {
        private static final String MDC_TRACE_ID = "traceId";

        private MdcTestMethodIdRepository() {
        }

        private static Optional<String> load() {
            String traceId = MDC.get(MDC_TRACE_ID);
            return TestMethodIdSupport.isValidTestMethodHashId(traceId) ? Optional.of(traceId) : Optional.empty();
        }
    }

    @SafeVarargs
    private static <T> Optional<T> or(Supplier<Optional<T>>... suppliers) {
        return Stream.of(suppliers)
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
