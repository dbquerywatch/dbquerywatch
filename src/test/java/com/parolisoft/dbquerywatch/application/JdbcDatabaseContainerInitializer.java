package com.parolisoft.dbquerywatch.application;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.annotation.Nonnull;
import java.util.Map;

abstract class JdbcDatabaseContainerInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected static void initialize(
        @Nonnull ConfigurableApplicationContext applicationContext,
        JdbcDatabaseContainer<?> jdbcDatabaseContainer
    ) {
        jdbcDatabaseContainer.start();
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
