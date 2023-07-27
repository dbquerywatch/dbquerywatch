package org.dbquerywatch;

import org.dbquerywatch.internal.CleanRuntimeException;

/**
 * Thrown when a non-specific error occurs during the EP analyzer execution.
 */
public class ExecutionPlanAnalyzerException extends CleanRuntimeException {

    /**
     * Creates the self-describing exception.
     *
     * @param detailMessage The detail message.
     */
    public ExecutionPlanAnalyzerException(String detailMessage) {
        super(detailMessage);
    }
}
