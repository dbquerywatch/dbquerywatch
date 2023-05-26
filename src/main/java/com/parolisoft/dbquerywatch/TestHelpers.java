package com.parolisoft.dbquerywatch;

import com.parolisoft.dbquerywatch.internal.ClassIdSupport;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Utility methods to simplify the eventual changes on user's integration tests.
 */
@UtilityClass
public class TestHelpers {

    /**
     * Generate the hash ID for a given test class, compatible with 128 bit traceId.
     *
     * @param clazz The wanted test class.
     * @return The hash ID as a 32-chars hex string.
     */
    public static String generateClassId(Class<?> clazz) {
        return ClassIdSupport.generateClassId(clazz);
    }

    /**
     * Create additional HTTP headers to enable the matching query/testClass via tracing.
     *
     * @param clazz The test class.
     * @param spanId The optional spanId. If null, a new random (8 bytes) will be generated.
     * @return All headers required to support both W3C (OpenTelemetry) and Brave tracers.
     */
    public static Map<String, String> buildTraceHeaders(Class<?> clazz, @Nullable String spanId) {
        String traceId = generateClassId(clazz);
        if (spanId == null) {
            spanId = generateRandomSpanId();
        }
        Map<String, String> headers = new HashMap<>();
        // OpenTelemetry / W3C
        // @see https://w3c.github.io/trace-context/#traceparent-header
        headers.put("traceparent", String.format("00-%s-%s-00", traceId, spanId));
        // brave (128 bit)
        // @see https://github.com/openzipkin/b3-propagation/blob/master/STATUS.md
        headers.put("X-B3-TraceId", traceId);
        headers.put("X-B3-SpanId", spanId);
        return headers;
    }

    private static String generateRandomSpanId() {
        byte[] spanIdBytes = new byte[8];
        new Random().nextBytes(spanIdBytes);
        return String.format("%016x", new BigInteger(1, spanIdBytes));
    }
}
