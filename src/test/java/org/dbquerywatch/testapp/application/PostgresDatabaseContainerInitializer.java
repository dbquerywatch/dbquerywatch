package org.dbquerywatch.testapp.application;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.annotation.Nonnull;
import java.util.Map;

class PostgresDatabaseContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @SuppressWarnings("resource")
    private static final JdbcDatabaseContainerInitializer SINGLETON_INITIALIZER = new JdbcDatabaseContainerInitializer(
        PostgreSQLContainer.NAME,
        new PostgreSQLContainer<>("postgres:15.3-alpine")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
            .withCommand(
                "postgres",
                // There is no need to flush data to disk.
                "-c", "fsync=off",
                // Discourages the planner from using sequential scan plan types.
                "-c", "enable_seqscan=off"
            )
    );

    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        SINGLETON_INITIALIZER.initialize(applicationContext);
    }
}
