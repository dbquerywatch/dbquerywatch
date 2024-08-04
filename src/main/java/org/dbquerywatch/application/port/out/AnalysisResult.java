package org.dbquerywatch.application.port.out;

import org.immutables.value.Value;
import org.dbquerywatch.application.domain.model.Issue;

import java.util.List;

@Value.Immutable
@Value.Style(allParameters = true)
public interface AnalysisResult {
    String getExecutionPlan();

    List<Issue> getIssues();
}
