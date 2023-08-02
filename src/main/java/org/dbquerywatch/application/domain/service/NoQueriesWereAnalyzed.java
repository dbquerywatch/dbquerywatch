package org.dbquerywatch.application.domain.service;

import org.dbquerywatch.common.CleanRuntimeException;

/**
 * Thrown when no analyzable query was effectively analyzed, most likely due to some misconfiguration.
 */
public class NoQueriesWereAnalyzed extends CleanRuntimeException {

    /**
     * Creates the self-describing exception.
     */
    public NoQueriesWereAnalyzed() {
        super("No queries were analyzed and/or associated to this test class. Possible causes:\n" +
            "  1. No database operation were actually exercised by your tests. If that's the case, just remove the @CatchSlowQueries annotation from your test class;\n" +
            "  2. You need to enable tracing in your integration tests. Popular choices are Spring Sleuth for Spring Boot 2.7, or Micrometer for Spring Boot 3+."
        );
    }
}
