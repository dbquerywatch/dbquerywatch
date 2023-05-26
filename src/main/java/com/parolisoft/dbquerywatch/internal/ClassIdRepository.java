package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Optional;

@ExtensionMethod({Optional.class, Optionals.class})
@UtilityClass
public class ClassIdRepository {

    public Optional<String> load() {
        return ThreadLocalClassIdRepository.load()
            .or(MdcClassIdRepository::load);
    }

    public void save(Class<?> clazz) {
        ThreadLocalClassIdRepository.save(clazz);
    }

    public void clear() {
        ThreadLocalClassIdRepository.clear();
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
}
