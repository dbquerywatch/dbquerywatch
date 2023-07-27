package org.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.internal.jdbc.JdbcClient;

import java.util.List;

public interface ExecutionPlanAnalyzer {

    JdbcClient getJdbcClient();

    void checkConfiguration();

    AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations);
}
