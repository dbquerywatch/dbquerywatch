package com.parolisoft.dbquerywatch.application;

import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.OracleContainer;

import javax.annotation.Nonnull;

class OracleDatabaseContainerInitializer extends JdbcDatabaseContainerInitializer {

    static final OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-xe:21.3.0-slim-faststart")
        .withReuse(true);

    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        initialize(applicationContext, oracleContainer);
    }
}
