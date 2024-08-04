package org.dbquerywatch.application.domain.model;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Value.Immutable
@SuppressWarnings("java:S1948")  // we are only using serializable implementations of Set/List for these fields
public interface SlowQueryReport extends Serializable {
    NamedDataSource getNamedDataSource();
    String getQuerySql();
    String getExecutionPlan();
    Set<String> getMethods();
    List<Issue> getIssues();
}
