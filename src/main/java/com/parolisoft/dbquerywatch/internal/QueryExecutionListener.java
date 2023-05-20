package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;

import javax.sql.DataSource;
import java.util.List;

public class QueryExecutionListener implements net.ttddyy.dsproxy.listener.QueryExecutionListener {

    private final ExecutionPlanAnalyzer analyzer;

    public QueryExecutionListener(String dataSourceName, DataSource dataSource) {
        this.analyzer = ExecutionPlanAnalyzerFactory.create(dataSourceName, dataSource);
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
