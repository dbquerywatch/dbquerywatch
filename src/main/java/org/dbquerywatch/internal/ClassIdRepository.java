package org.dbquerywatch.internal;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

@UtilityClass
public class ClassIdRepository {

    private static final AtomicInteger THREAD_LOCAL_HITS = new AtomicInteger();
    private static final AtomicInteger MDC_HITS = new AtomicInteger();

    public Optional<String> load() {
        return or(
            () -> meteredOptional(ThreadLocalClassIdRepository.load(), THREAD_LOCAL_HITS),
            () -> meteredOptional(MdcClassIdRepository.load(), MDC_HITS)
        );
    }

    public void save(Class<?> clazz) {
        ThreadLocalClassIdRepository.save(clazz);
    }

    public void clear() {
        ThreadLocalClassIdRepository.clear();
    }

    public void resetMetrics() {
        THREAD_LOCAL_HITS.getAndSet(0);
        MDC_HITS.getAndSet(0);
    }

    public int getThreadLocalHits() {
        return THREAD_LOCAL_HITS.get();
    }

    public int getMdcHits() {
        return MDC_HITS.get();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T> Optional<T> meteredOptional(Optional<T> optional, AtomicInteger hits) {
        if (optional.isPresent()) {
            hits.incrementAndGet();
        }
        return optional;
    }

    @UtilityClass
    private static final class ThreadLocalClassIdRepository {

        private static final ThreadLocal<String> CLASS_ID = new ThreadLocal<>();

        private  Optional<String> load() {
            return Optional.ofNullable(CLASS_ID.get());
        }

        private  void save(Class<?> clazz) {
            CLASS_ID.set(ClassIdSupport.generateClassId(clazz));
        }

        private  void clear() {
            CLASS_ID.remove();
        }
    }

    @UtilityClass
    private static class MdcClassIdRepository {
        private static final String MDC_TRACE_ID = "traceId";

        private Optional<String> load() {
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
