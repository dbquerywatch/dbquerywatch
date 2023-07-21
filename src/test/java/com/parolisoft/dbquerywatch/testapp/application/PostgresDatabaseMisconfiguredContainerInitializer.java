package com.parolisoft.dbquerywatch.testapp.application;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.annotation.Nonnull;
import java.util.Map;

class PostgresDatabaseMisconfiguredContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @SuppressWarnings("resource")
    private static final JdbcDatabaseContainerInitializer SINGLETON_INITIALIZER = new JdbcDatabaseContainerInitializer(
        PostgreSQLContainer.NAME,
        new PostgreSQLContainer<>("postgres:15.3-alpine")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
    );

    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        SINGLETON_INITIALIZER.initialize(applicationContext);
    }
}
