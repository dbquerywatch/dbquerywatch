package com.parolisoft.dbquerywatch.spring;

import com.parolisoft.dbquerywatch.TestHelpers;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

/**
 * Spring-specific utility methods to simplify the eventual changes on user's integration tests.
 */
@UtilityClass
public class SpringTestHelpers {

    /**
     * Generate the HTTP tracing headers for a given test class and add them to an existing HttpHeaders object.
     *
     * @param headers The target HttpHeaders object.
     * @param clazz The test class from which the headers will be generated.
     */
    public void addTraceHeaders(HttpHeaders headers, Class<?> clazz) {
        TestHelpers.buildTraceHeaders(clazz, null).forEach(headers::add);
    }
}
