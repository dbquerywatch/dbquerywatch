package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.List;

public class QueryExecutionListener implements net.ttddyy.dsproxy.listener.QueryExecutionListener {

    private final ExecutionPlanAnalyzer analyzer;

    public QueryExecutionListener(Environment environment, String name, DataSource dataSource) {
        AnalyzerSettings settings = AnalyzerSettings.from(environment);
        this.analyzer = ExecutionPlanAnalyzerFactory.create(settings, name, dataSource);
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            String querySql = queryInfo.getQuery();
            ExecutionPlanManager.afterQuery(analyzer, querySql, queryInfo.getParametersList());
        }
    }
}
