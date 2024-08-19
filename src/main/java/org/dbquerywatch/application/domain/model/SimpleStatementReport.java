package org.dbquerywatch.application.domain.model;

import java.util.Set;

public class SimpleStatementReport implements StatementReport {
    private final NamedDataSource namedDataSource;
    private final String sqlStatement;
    private final String executionPlan;
    private final long totalCost;
    private final Set<String> methods;

    public SimpleStatementReport(
        NamedDataSource namedDataSource,
        String sqlStatement,
        String executionPlan,
        long totalCost,
        Set<String> methods
    ) {
        this.namedDataSource = namedDataSource;
        this.sqlStatement = sqlStatement;
        this.executionPlan = executionPlan;
        this.totalCost = totalCost;
        this.methods = methods;
    }

    @Override
    public NamedDataSource getNamedDataSource() {
        return namedDataSource;
    }

    @Override
    public String getSqlStatement() {
        return sqlStatement;
    }

    @Override
    public String getExecutionPlan() {
        return executionPlan;
    }

    @Override
    public long getTotalCost() {
        return totalCost;
    }

    @Override
    public Set<String> getMethods() {
        return methods;
    }
}
