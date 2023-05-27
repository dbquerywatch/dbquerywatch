package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import com.parolisoft.dbquerywatch.internal.spring.AnalyzerSettingsAdapter;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.springframework.core.env.Environment;

import java.util.List;

public class QueryExecutionListener implements net.ttddyy.dsproxy.listener.QueryExecutionListener {

    private final ExecutionPlanAnalyzer analyzer;
    private final AnalyzerSettingsAdapter analyzerSettings;

    public QueryExecutionListener(Environment environment, JdbcClient jdbcClient) {
        this.analyzerSettings = new AnalyzerSettingsAdapter(environment);
        this.analyzer = ExecutionPlanAnalyzerFactory.create(jdbcClient);
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        for (QueryInfo queryInfo : queryInfoList) {
            String querySql = queryInfo.getQuery();
            ExecutionPlanManager.afterQuery(analyzer, analyzerSettings, querySql, queryInfo.getParametersList());
        }
    }
}
