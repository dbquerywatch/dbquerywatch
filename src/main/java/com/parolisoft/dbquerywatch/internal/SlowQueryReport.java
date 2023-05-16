package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
public class SlowQueryReport {
    String dataSourceName;
    String querySql;
    Set<String> methods;
    List<Issue> issues;
}
