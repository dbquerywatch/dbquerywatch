package com.parolisoft.dbquerywatch.internal;

import lombok.Value;
import org.junit.jupiter.api.Named;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

@Value
public class SlowQueryReport {
    Named<DataSource> namedDataSource;
    String querySql;
    String executionPlan;
    Set<String> methods;
    List<Issue> issues;
}
