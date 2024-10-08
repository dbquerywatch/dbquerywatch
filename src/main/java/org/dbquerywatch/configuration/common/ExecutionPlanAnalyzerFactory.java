package org.dbquerywatch.configuration.common;

import org.dbquerywatch.adapters.out.analyzers.H2ExecutionPlanAnalyzer;
import org.dbquerywatch.adapters.out.analyzers.MySQLExecutionPlanAnalyzer;
import org.dbquerywatch.adapters.out.analyzers.OracleExecutionPlanAnalyzer;
import org.dbquerywatch.adapters.out.analyzers.PostgresExecutionPlanAnalyzer;
import org.dbquerywatch.application.port.out.ExecutionPlanAnalyzer;
import org.dbquerywatch.application.port.out.JdbcClient;

public final class ExecutionPlanAnalyzerFactory {
    private ExecutionPlanAnalyzerFactory() {
    }

    public static ExecutionPlanAnalyzer create(JdbcClient jdbcClient) {
        String productName = jdbcClient.getNamedDataSource().getProductName();
        switch (productName) {
            case "H2":
                return new H2ExecutionPlanAnalyzer(jdbcClient);
            case "MySQL":
                return new MySQLExecutionPlanAnalyzer(jdbcClient);
            case "Oracle":
                return new OracleExecutionPlanAnalyzer(jdbcClient);
            case "PostgreSQL":
                return new PostgresExecutionPlanAnalyzer(jdbcClient);
            default:
                throw new IllegalArgumentException("Unknown DB product dataSourceName: " + productName);
        }
    }
}
