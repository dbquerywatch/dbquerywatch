package org.dbquerywatch.api.helpers;

import org.springframework.http.HttpHeaders;

/**
 * Spring-specific utility methods to simplify the eventual changes on user's integration tests.
 */
public final class SpringTestHelpers {
    private SpringTestHelpers() {
    }

    /**
     * Generate the HTTP tracing headers for a given test class and add them to an existing HttpHeaders object.
     *
     * @param headers The target HttpHeaders object.
     * @param clazz The test class from which the headers will be generated.
     */
    public static void addTraceHeaders(HttpHeaders headers, Class<?> clazz) {
        TestHelpers.buildTraceHeaders(clazz, null).forEach(headers::add);
    }
}
