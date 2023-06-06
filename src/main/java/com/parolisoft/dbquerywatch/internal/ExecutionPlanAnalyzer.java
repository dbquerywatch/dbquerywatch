package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.List;

interface ExecutionPlanAnalyzer {

    JdbcClient getJdbcClient();

    void checkConfiguration();

    AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations);
}
