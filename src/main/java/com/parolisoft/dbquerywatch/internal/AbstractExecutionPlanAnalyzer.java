package com.parolisoft.dbquerywatch.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
@Getter
abstract class AbstractExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    @Nonnull
    private final String name;

    @Nonnull
    private final AnalyzerSettings settings;

    @Nonnull
    protected final JdbcTemplate jdbcTemplate;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExecutionPlanAnalyzer) {
            return this.name.equals(((ExecutionPlanAnalyzer) obj).getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("analyzer[%s]", name);
    }
}
