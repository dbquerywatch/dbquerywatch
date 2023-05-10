package com.parolisoft.dbquerywatch;

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
        return switch (productName) {
            case "Oracle" -> new OracleExecutionPlanAnalyzer(jdbcTemplate);
            case "PostgreSQL" -> new PostgresExecutionPlanAnalyzer(jdbcTemplate);
            default -> throw new IllegalArgumentException("Unknown DB product name: " + productName);
        };
    }

}
