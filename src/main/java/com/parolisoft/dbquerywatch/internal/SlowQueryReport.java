package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Value
public class SlowQueryReport {
    @Nonnull String dataSourceName;
    @Nonnull String querySql;
    @Nonnull String executionPlan;
    @Nonnull Set<String> methods;
    @Nonnull List<Issue> issues;
}
