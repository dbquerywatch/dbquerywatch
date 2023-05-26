package com.parolisoft.dbquerywatch.application;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@ActiveProfiles("postgres")
@Disabled("Go to junit-platform.properties to re-enable all disabled tests at once.")
public class PostgresIntegrationTests extends IntegrationTests {

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
        .withReuse(true);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        postgresContainer.start();
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
}
