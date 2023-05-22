package com.parolisoft.dbquerywatch.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlowQueriesFoundException extends CleanRuntimeException {
    private final List<SlowQueryReport> slowQueries;

    public SlowQueriesFoundException(List<SlowQueryReport> slowQueries) {
        super(describe(slowQueries));
        this.slowQueries = new ArrayList<>(slowQueries);
    }

    public List<SlowQueryReport> getSlowQueries() {
        return Collections.unmodifiableList(slowQueries);
    }

    private static String describe(List<SlowQueryReport> slowQueries) {
        StringBuilder sb = new StringBuilder("Potential slow queries were found!\n");
        for (int i = 0; i < slowQueries.size(); i++) {
            SlowQueryReport slowQuery = slowQueries.get(i);
            sb.append('\n')
                .append(heading("Query %d/%d", i + 1, slowQueries.size()))
                .append("\nSQL:\n    ")
                .append(slowQuery.getQuerySql());
            sb.append("\nExecution Plan:\n    ")
                .append(slowQuery.getExecutionPlan());
            sb.append("\nIssues:");
            for (Issue issue : slowQuery.getIssues()) {
                sb.append("\n    - ")
                    .append(issue.toString());
            }
            sb.append("\nCaller Methods:");
            for (String methodName : slowQuery.getMethods()) {
                sb.append("\n    - ")
                    .append(methodName);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static final String DASHES = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

    @SuppressWarnings("SameParameterValue")
    private static String heading(String fmt, Object... args) {
        String title = String.format(fmt, args);
        int padLen = (80 - 7) - title.length();
        //noinspection MalformedFormatString
        return String.format("%.5s %s %." + padLen + "s", DASHES, title, DASHES);
    }
}
