package com.parolisoft.dbquerywatch.application;

import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

import javax.annotation.Nonnull;
import java.util.Map;

class MySQLDatabaseContainerInitializer extends JdbcDatabaseContainerInitializer {

    @SuppressWarnings("resource")
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
        .withTmpFs(Map.of("/var/lib/mysql", "rw"))
        .withReuse(true);

    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext applicationContext) {
        initialize(applicationContext, mysqlContainer);
    }
}
