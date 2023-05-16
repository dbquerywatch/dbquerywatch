package com.parolisoft.dbquerywatch.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"dataSourceName"})
@ToString(of = {"dataSourceName"})
@Getter
abstract class AbstractExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    @Nonnull
    private final String dataSourceName;

    @Nonnull
    private final AnalyzerSettings settings;

    @Nonnull
    protected final JdbcTemplate jdbcTemplate;
}
