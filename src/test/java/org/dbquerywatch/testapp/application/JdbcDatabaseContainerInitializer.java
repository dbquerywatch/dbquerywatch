package org.dbquerywatch.testapp.application;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.lifecycle.Startables;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class JdbcDatabaseContainerInitializer {

    private static final String REUSE_LABEL_ID = "io.dbquerywatch.reuse-id";

    private final JdbcDatabaseContainer<?> jdbcDatabaseContainer;
    private final CompletableFuture<Void> startFuture;

    JdbcDatabaseContainerInitializer(String name, JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        this.jdbcDatabaseContainer = jdbcDatabaseContainer
            .withLabel(REUSE_LABEL_ID, name)
            .withReuse(true);
        this.startFuture = Startables.deepStart(this.jdbcDatabaseContainer);
    }

    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        startFuture.join();
        TestPropertyValues.of(
            Map.of(
                "spring.datasource.driver-class-name", jdbcDatabaseContainer.getDriverClassName(),
                "spring.datasource.url", jdbcDatabaseContainer.getJdbcUrl(),
                "spring.datasource.username", jdbcDatabaseContainer.getUsername(),
                "spring.datasource.password", jdbcDatabaseContainer.getPassword()
            )
        ).applyTo(applicationContext.getEnvironment());
    }
}
