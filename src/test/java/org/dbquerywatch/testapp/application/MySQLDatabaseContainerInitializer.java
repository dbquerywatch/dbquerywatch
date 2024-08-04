package org.dbquerywatch.testapp.application;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

class MySQLDatabaseContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @SuppressWarnings("resource")
    private static final JdbcDatabaseContainerInitializer SINGLETON_INITIALIZER = new JdbcDatabaseContainerInitializer(
        MySQLContainer.NAME,
        new MySQLContainer<>("mysql:8.0.33")
            .withTmpFs(Map.of("/var/lib/mysql", "rw"))
    );

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        SINGLETON_INITIALIZER.initialize(applicationContext);
    }
}
