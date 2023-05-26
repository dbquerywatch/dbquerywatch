package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import javax.sql.DataSource;
import java.util.List;

interface ExecutionPlanAnalyzer {

    DataSource getDataSource();

    String getDataSourceName();

    AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations);
}
