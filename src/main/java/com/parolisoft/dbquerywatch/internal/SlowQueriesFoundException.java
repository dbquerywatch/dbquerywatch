package com.parolisoft.dbquerywatch.internal;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlowQueriesFoundException extends CleanRuntimeException {

    private static final String LF_INDENT = "\n    ";

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
                .append("\nDataSource:")
                .append(LF_INDENT).append(slowQuery.getDataSourceName())
                .append(" (").append(getUrl(slowQuery.getDataSource())).append(')')
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

    private static final String DASHES = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

    @SuppressWarnings("SameParameterValue")
    private static String heading(String fmt, Object... args) {
        String title = String.format(fmt, args);
        int padLen = (80 - 7) - title.length();
        //noinspection MalformedFormatString
        return String.format("%.5s %s %." + padLen + "s", DASHES, title, DASHES);
    }

    private static String getUrl(DataSource ds) {
        try {
            try (Connection connection = ds.getConnection()) {
                return connection.getMetaData().getURL();
            }
        } catch (Exception ignored) {}
        return "UNKNOWN URL";
    }
}
