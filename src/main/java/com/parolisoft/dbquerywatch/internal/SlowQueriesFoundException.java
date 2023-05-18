package com.parolisoft.dbquerywatch.internal;

import java.util.Collections;
import java.util.List;

public class SlowQueriesFoundException extends CleanRuntimeException {
    private final List<SlowQueryReport> slowQueries;

    public SlowQueriesFoundException(List<SlowQueryReport> slowQueries) {
        super(describe(slowQueries));
        this.slowQueries = Collections.unmodifiableList(slowQueries);
    }

    public List<SlowQueryReport> getSlowQueries() {
        return slowQueries;
    }

    private static String describe(List<SlowQueryReport> slowQueries) {
        StringBuilder sb = new StringBuilder("Potential slow queries were found\n");
        for (int i = 0; i < slowQueries.size(); i++) {
            SlowQueryReport slowQuery = slowQueries.get(i);
            sb.append(String.format("\nQuery %d/%d:\n    ", i + 1, slowQueries.size()))
                .append(slowQuery.getQuerySql());
            sb.append("\nIssues:");
            for (Issue issue : slowQuery.getIssues()) {
                sb.append("\n    - ")
                    .append(issue.toString());
            }
            sb.append("\nTest methods:");
            for (String methodName : slowQuery.getMethods()) {
                sb.append("\n    - ")
                    .append(methodName)
                    .append("()");
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}
