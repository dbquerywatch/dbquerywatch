package com.parolisoft.dbquerywatch.application;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("h2")
@Disabled("Go to junit-platform.properties to re-enable all disabled tests at once.")
public class H2IntegrationTests extends IntegrationTests {
}
