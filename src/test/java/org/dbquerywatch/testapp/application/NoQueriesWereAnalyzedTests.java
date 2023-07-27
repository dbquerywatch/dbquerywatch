package org.dbquerywatch.testapp.application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Disabled("Expected to fail. Go to junit-platform.properties to re-enable all disabled tests at once.")
class NoQueriesWereAnalyzedTests extends BaseIntegrationTests {

    @Test
    void dummy_test() {
        // Copied from a production code I saw a while ago and I can't just unsee :-(
        assertTrue(true);
    }
}
