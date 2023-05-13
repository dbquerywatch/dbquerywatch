package com.parolisoft.dbquerywatch.internal;

import java.util.List;

interface ExecutionPlanAnalyzer {

    List<Issue> analyze(String statementId, String querySql, Object[] args);
}
