package org.dbquerywatch.testapp.application;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.OracleContainer;

class OracleDatabaseContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final JdbcDatabaseContainerInitializer SINGLETON_INITIALIZER = new JdbcDatabaseContainerInitializer(
        OracleContainer.NAME,
        new OracleContainer("gvenzl/oracle-xe:21.3.0-slim-faststart")
    );

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        SINGLETON_INITIALIZER.initialize(applicationContext);
    }
}
