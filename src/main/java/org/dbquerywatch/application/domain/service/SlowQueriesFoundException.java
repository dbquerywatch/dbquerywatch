package org.dbquerywatch.application.domain.service;

import com.google.errorprone.annotations.FormatMethod;
import org.dbquerywatch.application.domain.model.Issue;
import org.dbquerywatch.application.domain.model.SlowQueryReport;
import org.dbquerywatch.common.CleanRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when one or multiple issues are found on the queries performed by the integration tests.
 */
public class SlowQueriesFoundException extends CleanRuntimeException {

    private static final String LF_INDENT = "\n    ";

    /**
     * Holds the provided SlowQueryReport items.
     */
    private final List<SlowQueryReport> slowQueries;

    /**
     * Creates an instance of the exception based on collection of SlowQueryReport items.
     *
     * @param slowQueries The SlowQueryReport items.
     */
    public SlowQueriesFoundException(List<SlowQueryReport> slowQueries) {
        super(describe(slowQueries));
        this.slowQueries = new ArrayList<>(slowQueries);
    }

    /**
     * Retrieves the SlowQueryReport items.
     *
     * @return The SlowQueryReport items.
     */
    public List<SlowQueryReport> getSlowQueries() {
        return Collections.unmodifiableList(slowQueries);
    }

    private static String describe(List<SlowQueryReport> slowQueries) {
        StringBuilder sb = new StringBuilder("Potential slow queries were found!\n");
        for (int i = 0; i < slowQueries.size(); i++) {
            SlowQueryReport slowQuery = slowQueries.get(i);
            sb.append('\n')
                .append(heading("Query %d/%d", i + 1, slowQueries.size()))
                .append("\nDataSource:")
                .append(LF_INDENT).append(slowQuery.getDataSourceName())
                .append("\nSQL:")
                .append(LF_INDENT).append(slowQuery.getQuerySql());
            sb.append("\nExecution Plan:")
                .append(LF_INDENT).append(slowQuery.getExecutionPlan());
            sb.append("\nIssues:");
            for (Issue issue : slowQuery.getIssues()) {
                sb.append(LF_INDENT).append("- ")
                    .append(issue.toString());
            }
            sb.append("\nCaller Methods:");
            for (String methodName : slowQuery.getMethods()) {
                sb.append(LF_INDENT).append("- ")
                    .append(methodName);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @FormatMethod
    @SuppressWarnings("SameParameterValue")
    private static String heading(String fmt, Object... args) {
        String title = String.format(fmt, args);
        char[] chars = new char[80];
        Arrays.fill(chars, '-');
        int index = 5;
        chars[index++] = '|';
        chars[index++] = ' ';
        for (int i = 0; i < title.length(); i++) {
            chars[index++] = title.charAt(i);
        }
        chars[index++] = ' ';
        chars[index] = '|';
        return new String(chars);
    }
}
