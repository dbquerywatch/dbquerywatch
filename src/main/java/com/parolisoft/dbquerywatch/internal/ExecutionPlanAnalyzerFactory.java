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
    ExecutionPlanAnalyzer create(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String productName = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        switch (productName) {
            case "H2":
                return new H2ExecutionPlanAnalyzer(jdbcTemplate);
            case "Oracle":
                return new OracleExecutionPlanAnalyzer(jdbcTemplate);
            case "PostgreSQL":
                return new PostgresExecutionPlanAnalyzer(jdbcTemplate);
            default:
                throw new IllegalArgumentException("Unknown DB product name: " + productName);
        }
    }

}