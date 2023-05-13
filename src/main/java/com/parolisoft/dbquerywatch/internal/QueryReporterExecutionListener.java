package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class QueryReporterExecutionListener implements QueryExecutionListener {

    private final ExecutionPlanReporter reporter;

    public QueryReporterExecutionListener(Environment environment, DataSource dataSource) {
        this.reporter = ExecutionPlanReporter.create(environment, dataSource);
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            reporter.report(
                    queryInfo.getQuery(),
                    () -> queryInfo.getParametersList().stream()
                            .findFirst()
                            .map(list -> list.stream().flatMap(ps -> Arrays.stream(ps.getArgs()).skip(1)))
                            .orElse(Stream.empty())
                            .toArray()
            );
        }
    }
}
