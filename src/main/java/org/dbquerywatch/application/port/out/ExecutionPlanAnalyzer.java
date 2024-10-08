package org.dbquerywatch.application.port.out;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.List;

public interface ExecutionPlanAnalyzer {

    JdbcClient getJdbcClient();

    void checkConfiguration();

    AnalysisReport analyze(String querySql, List<ParameterSetOperation> operations);
}
