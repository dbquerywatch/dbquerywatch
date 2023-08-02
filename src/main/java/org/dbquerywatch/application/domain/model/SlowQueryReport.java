package org.dbquerywatch.application.domain.model;

import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Value
@SuppressWarnings("java:S1948")  // we are only using serializable implementations of Set/List for these fields
public class SlowQueryReport implements Serializable {
    String dataSourceName;
    String querySql;
    String executionPlan;
    Set<String> methods;
    List<Issue> issues;
}
