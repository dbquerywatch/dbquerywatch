package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.List;

interface ExecutionPlanAnalyzer {

    String getDataSourceName();

    AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations);
}
