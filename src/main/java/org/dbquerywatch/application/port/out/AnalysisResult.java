package org.dbquerywatch.application.port.out;

import lombok.Value;
import org.dbquerywatch.application.domain.model.Issue;

import java.util.List;

@Value
public class AnalysisResult {
    String executionPlan;
    List<Issue> issues;
}
