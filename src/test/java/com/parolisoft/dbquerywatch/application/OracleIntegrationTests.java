package com.parolisoft.dbquerywatch.application;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;

@ActiveProfiles("oracle")
@Disabled("Go to junit-platform.properties to re-enable all disabled tests at once.")
public class OracleIntegrationTests extends IntegrationTests {

    static final OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-xe:21.3.0-slim-faststart")
        .withReuse(true);

    @DynamicPropertySource
    static void oracleProperties(DynamicPropertyRegistry registry) {
        oracleContainer.start();
        registry.add("spring.datasource.driver-class-name", oracleContainer::getDriverClassName);
        registry.add("spring.datasource.url", oracleContainer::getJdbcUrl);
        registry.add("spring.datasource.username", oracleContainer::getUsername);
        registry.add("spring.datasource.password", oracleContainer::getPassword);
    }
}
