package org.dbquerywatch.api.helpers;

import org.dbquerywatch.application.domain.service.TestMethodIdSupport;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Utility methods to simplify the eventual changes on user's integration tests.
 */
public final class TestHelpers {

    private static final Random RND = new SecureRandom();

    private TestHelpers() {
    }

    /**
     * Generate the hash ID for a given test class, compatible with 128 bit traceId.
     *
     * @param uniqueId  Unique ID of the test method.
     * @return The hash ID as a 32-chars hex string.
     */
    public static String generateTestMethodId(String uniqueId) {
        return TestMethodIdSupport.generateTestMethodId(uniqueId);
    }

    /**
     * Create additional HTTP headers to enable the matching query/testClass via tracing.
     *
     * @param uniqueId  Unique ID of the test method.
     * @param spanId The optional spanId. If null, a new random (8 bytes) will be generated.
     * @return All headers required to support both W3C (OpenTelemetry) and Brave tracers.
     */
    public static Map<String, String> buildTraceHeaders(String uniqueId, @Nullable String spanId) {
        String traceId = generateTestMethodId(uniqueId);
        String actualSpanId = (spanId != null) ? spanId : generateRandomSpanId();
        Map<String, String> headers = new HashMap<>();
        // OpenTelemetry / W3C
        // @see https://w3c.github.io/trace-context/#traceparent-header
        headers.put("traceparent", String.format("00-%s-%s-00", traceId, actualSpanId));
        // brave (128 bit)
        // @see https://github.com/openzipkin/b3-propagation/blob/master/STATUS.md
        headers.put("X-B3-TraceId", traceId);
        headers.put("X-B3-SpanId", actualSpanId);
        return headers;
    }

    private static String generateRandomSpanId() {
        return String.format("%016x", new BigInteger(8 * Byte.SIZE, RND));
    }
}
