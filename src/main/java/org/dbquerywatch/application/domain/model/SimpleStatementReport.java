package org.dbquerywatch.application.domain.model;

import java.util.Set;

public class SimpleStatementReport implements StatementReport {
    private final NamedDataSource namedDataSource;
    private final String sqlStatement;
    private final String executionPlan;
    private final Set<String> methods;

    public SimpleStatementReport(
        NamedDataSource namedDataSource,
        String sqlStatement,
        String executionPlan,
        Set<String> methods
    ) {
        this.namedDataSource = namedDataSource;
        this.sqlStatement = sqlStatement;
        this.executionPlan = executionPlan;
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
    public Set<String> getMethods() {
        return methods;
    }
}
