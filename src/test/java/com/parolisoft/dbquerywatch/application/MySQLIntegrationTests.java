package com.parolisoft.dbquerywatch.application;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

@ActiveProfiles("mysql")
@Disabled("Go to junit-platform.properties to re-enable all disabled tests at once.")
public class MySQLIntegrationTests extends IntegrationTests {

    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
        .withTmpFs(Map.of("/var/lib/mysql", "rw"))
        .withReuse(true);

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        mysqlContainer.start();
        registry.add("spring.datasource.driver-class-name", mysqlContainer::getDriverClassName);
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }
}
