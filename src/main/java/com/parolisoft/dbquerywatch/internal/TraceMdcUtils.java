package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Optional;

@UtilityClass
public class TraceMdcUtils {
    private static final String MDC_TRACE_ID = "traceId";

    public static void setTestClass(Class<?> clazz) {
        MDC.put(MDC_TRACE_ID, ClassHashSupport.classHashId(clazz));
    }

    public static Optional<String> getTestClassHashId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        return ClassHashSupport.isValidClassHashId(traceId) ? Optional.of(traceId) : Optional.empty();
    }

    public static void clearTestClass() {
        getTestClassHashId().ifPresent(ignored -> MDC.remove(MDC_TRACE_ID));
    }
}
