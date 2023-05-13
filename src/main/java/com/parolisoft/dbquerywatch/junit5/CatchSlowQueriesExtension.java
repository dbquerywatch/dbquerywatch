package com.parolisoft.dbquerywatch.junit5;

import com.parolisoft.dbquerywatch.internal.SlowQueriesCatcher;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that checks if any query executed during each test was detected as potentially slow.
 *
 */
@Slf4j
public class CatchSlowQueriesExtension implements BeforeEachCallback, AfterEachCallback {

    private final SlowQueriesCatcher catcher = new SlowQueriesCatcher();

    @Override
    public void beforeEach(ExtensionContext context) {
        catcher.beforeEach();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        catcher.afterEach();
    }
}
