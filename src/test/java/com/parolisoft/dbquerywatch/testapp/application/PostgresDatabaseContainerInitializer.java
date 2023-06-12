package com.parolisoft.dbquerywatch.testapp.application;

import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.annotation.Nonnull;
import java.util.Map;

class PostgresDatabaseContainerInitializer extends JdbcDatabaseContainerInitializer {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.3-alpine")
        .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
        .withCommand(
            "postgres",
            // There is no need to flush data to disk.
            "-c", "fsync=off",
            // Discourages the planner from using sequential scan plan types.
            "-c", "enable_seqscan=off"
        )
        .withLabel(REUSE_LABEL_ID, reuseLabelValue(PostgreSQLContainer.NAME))
        .withReuse(true);

    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        initialize(applicationContext, postgresContainer);
    }
}
