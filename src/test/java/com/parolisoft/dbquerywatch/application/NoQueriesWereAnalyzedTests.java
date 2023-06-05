package com.parolisoft.dbquerywatch.application;

import com.parolisoft.dbquerywatch.junit5.CatchSlowQueries;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@CatchSlowQueries
@Disabled("Expected to fail. Go to junit-platform.properties to re-enable all disabled tests at once.")
public class NoQueriesWereAnalyzedTests {

    @Test
    void dummy_test() {
        // Copied from a production code I saw a while ago and I can't just unsee :-(
        assertTrue(true);
    }
}
