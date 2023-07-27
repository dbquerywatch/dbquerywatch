package org.dbquerywatch.internal;

import lombok.Value;

import java.util.List;

@Value
class AnalysisResult {
    String executionPlan;
    List<Issue> issues;
}
