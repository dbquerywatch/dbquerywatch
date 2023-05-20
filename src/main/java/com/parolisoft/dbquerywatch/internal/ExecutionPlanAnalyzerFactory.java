package com.parolisoft.dbquerywatch.internal;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

@UtilityClass
class ExecutionPlanAnalyzerFactory {

    @SneakyThrows
    ExecutionPlanAnalyzer create(String dataSourceName, DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String productName = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        switch (productName) {
            case "H2":
                return new H2ExecutionPlanAnalyzer(dataSourceName, jdbcTemplate);
            case "MySQL":
                return new MySQLExecutionPlanAnalyzer(dataSourceName, jdbcTemplate);
            case "Oracle":
                return new OracleExecutionPlanAnalyzer(dataSourceName, jdbcTemplate);
            case "PostgreSQL":
                return new PostgresExecutionPlanAnalyzer(dataSourceName, jdbcTemplate);
            default:
                throw new IllegalArgumentException("Unknown DB product dataSourceName: " + productName);
        }
    }

}
