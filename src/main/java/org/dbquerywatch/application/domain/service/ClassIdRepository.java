package org.dbquerywatch.application.domain.service;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ClassIdRepository {

    private static final AtomicInteger THREAD_LOCAL_HITS = new AtomicInteger();
    private static final AtomicInteger MDC_HITS = new AtomicInteger();

    private ClassIdRepository() {
    }

    static Optional<String> load() {
        return or(
            () -> meteredOptional(ThreadLocalClassIdRepository.load(), THREAD_LOCAL_HITS),
            () -> meteredOptional(MdcClassIdRepository.load(), MDC_HITS)
        );
    }

    public static void save(Class<?> clazz) {
        ThreadLocalClassIdRepository.save(clazz);
    }

    public static void clear() {
        ThreadLocalClassIdRepository.clear();
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

    private static final class ThreadLocalClassIdRepository {

        private static final ThreadLocal<String> CLASS_ID = new ThreadLocal<>();

        private ThreadLocalClassIdRepository() {
        }

        private static Optional<String> load() {
            return Optional.ofNullable(CLASS_ID.get());
        }

        private static void save(Class<?> clazz) {
            CLASS_ID.set(ClassIdSupport.generateClassId(clazz));
        }

        private static void clear() {
            CLASS_ID.remove();
        }
    }

    private static final class MdcClassIdRepository {
        private static final String MDC_TRACE_ID = "traceId";

        private MdcClassIdRepository() {
        }

        private static Optional<String> load() {
            String traceId = MDC.get(MDC_TRACE_ID);
            return ClassIdSupport.isValidClassHashId(traceId) ? Optional.of(traceId) : Optional.empty();
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
