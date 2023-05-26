package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

@Value
public class SlowQueryReport {
    String dataSourceName;
    DataSource dataSource;
    String querySql;
    String executionPlan;
    Set<String> methods;
    List<Issue> issues;
}
