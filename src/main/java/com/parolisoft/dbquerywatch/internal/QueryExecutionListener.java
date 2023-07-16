package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;

import java.util.List;
import java.util.function.Supplier;

public class QueryExecutionListener implements net.ttddyy.dsproxy.listener.QueryExecutionListener {

    private final ExecutionPlanAnalyzer analyzer;
    private final Supplier<AnalyzerSettings> analyzerSettingsSupplier;

    public QueryExecutionListener(Supplier<AnalyzerSettings> analyzerSettingsSupplier, JdbcClient jdbcClient) {
        this.analyzerSettingsSupplier = analyzerSettingsSupplier;
        this.analyzer = ExecutionPlanAnalyzerFactory.create(jdbcClient);
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            String querySql = queryInfo.getQuery();
            ExecutionPlanManager.afterQuery(analyzer, analyzerSettingsSupplier.get(), querySql,
                queryInfo.getParametersList());
        }
    }
}
