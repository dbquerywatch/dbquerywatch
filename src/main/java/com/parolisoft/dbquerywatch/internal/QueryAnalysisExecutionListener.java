package com.parolisoft.dbquerywatch.internal;

import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class QueryAnalysisExecutionListener implements QueryExecutionListener {

    private final Supplier<ExecutionPlanManager> executionPlanManagerSupplier;
    private final ExecutionPlanAnalyzer analyzer;

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        getExecutionPlanManager().ifPresent(executionPlanManager -> {
                for (QueryInfo queryInfo : queryInfoList) {
                    String querySql = queryInfo.getQuery();
                    executionPlanManager.afterQuery(analyzer, querySql, queryInfo.getParametersList());
                }
            }
        );
    }

    private Optional<ExecutionPlanManager> getExecutionPlanManager() {
        try {
            return Optional.of(executionPlanManagerSupplier.get());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
