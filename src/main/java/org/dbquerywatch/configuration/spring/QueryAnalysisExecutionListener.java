package org.dbquerywatch.configuration.spring;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.dbquerywatch.application.domain.service.ExecutionPlanManager;
import org.dbquerywatch.application.port.out.ExecutionPlanAnalyzer;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

class QueryAnalysisExecutionListener implements QueryExecutionListener {

    private final Supplier<ExecutionPlanManager> executionPlanManagerSupplier;
    private final ExecutionPlanAnalyzer analyzer;

    QueryAnalysisExecutionListener(Supplier<ExecutionPlanManager> executionPlanManagerSupplier, ExecutionPlanAnalyzer analyzer) {
        this.executionPlanManagerSupplier = executionPlanManagerSupplier;
        this.analyzer = analyzer;
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) { /* */ }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        getExecutionPlanManager().ifPresent(executionPlanManager -> {
                for (QueryInfo queryInfo : queryInfoList) {
                    String querySql = queryInfo.getQuery();
                    executionPlanManager.acceptQuery(analyzer, querySql, queryInfo.getParametersList());
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
