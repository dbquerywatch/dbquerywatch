package com.parolisoft.dbquerywatch.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
@Getter
abstract class AbstractExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    @Nonnull
    private final String name;

    @Nonnull
    private final AnalyzerSettings settings;

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

    @SneakyThrows
    protected static void setParameters(PreparedStatement ps, List<ParameterSetOperation> operations) {
        for (ParameterSetOperation operation : operations) {
            operation.getMethod().invoke(ps, operation.getArgs());
        }
    }
}
