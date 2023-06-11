package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Value
public class SlowQueryReport implements Serializable {
    String dataSourceName;
    String querySql;
    String executionPlan;
    Set<String> methods;
    List<Issue> issues;
}
