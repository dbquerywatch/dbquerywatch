package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.DatabaseMetaData;

@UtilityClass
class ExecutionPlanAnalyzerFactory {

    @SneakyThrows
    ExecutionPlanAnalyzer create(JdbcClient jdbcClient) {
        String productName = JdbcUtils.extractDatabaseMetaData(jdbcClient.getDataSource(),
            DatabaseMetaData::getDatabaseProductName);
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
