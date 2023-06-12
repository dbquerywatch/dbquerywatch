package com.parolisoft.dbquerywatch.testapp.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.parolisoft.dbquerywatch.internal.ClassIdRepository;
import com.parolisoft.dbquerywatch.junit5.CatchSlowQueries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.jdbc.Sql;

@Sql({"/data.sql"})
@CatchSlowQueries
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTests {

    @BeforeAll
    void clearMetrics() {
        ClassIdRepository.resetMetrics();
    }

    // avoid spurious log messages after all tests are ran
    @AfterAll
    void silentLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.WARN);
    }
}
