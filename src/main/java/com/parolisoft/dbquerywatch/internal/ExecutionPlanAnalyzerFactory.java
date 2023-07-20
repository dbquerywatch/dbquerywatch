package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

@UtilityClass
public class ExecutionPlanAnalyzerFactory {

    @SneakyThrows
    public ExecutionPlanAnalyzer create(JdbcClient jdbcClient) {
        DataSource dataSource = jdbcClient.getNamedDataSource().getPayload();
        String productName = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
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
