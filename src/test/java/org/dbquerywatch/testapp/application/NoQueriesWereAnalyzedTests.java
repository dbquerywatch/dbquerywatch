package org.dbquerywatch.testapp.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import nl.altindag.log.LogCaptor;
import org.dbquerywatch.api.spring.junit5.CatchSlowQueries;
import org.dbquerywatch.application.domain.service.ExecutionPlanManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(NoQueriesWereAnalyzedTests.VerificationExtension.class)
@CatchSlowQueries
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NoQueriesWereAnalyzedTests {

    private LogCaptor logCaptor;

    @BeforeEach
    void setLogCaptor() {
        logCaptor = LogCaptor.forClass(ExecutionPlanManager.class);
    }

    // avoid spurious log messages after all tests are ran
    @AfterAll
    void silentLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.WARN);
    }

    @Test
    void should_warn_if_no_queries_were_analyzed() {
        // Copied from a production code I saw a while ago and I can't just unsee :-(
        assertTrue(true);
    }

    static class VerificationExtension implements AfterAllCallback {
        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            NoQueriesWereAnalyzedTests instance = (NoQueriesWereAnalyzedTests) context.getRequiredTestInstance();
            LogCaptor logCaptor = instance.logCaptor;
            assertThat(logCaptor.getWarnLogs())
                .containsExactly("No query data found for class org.dbquerywatch.testapp.application.NoQueriesWereAnalyzedTests");
        }
    }
}
