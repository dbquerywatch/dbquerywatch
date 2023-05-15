package com.parolisoft.dbquerywatch.junit5;

import com.parolisoft.dbquerywatch.internal.ExecutionPlanManager;
import com.parolisoft.dbquerywatch.internal.TestMethodTracker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that checks if any query executed during each test was detected as potentially slow.
 *
 */
@Slf4j
public class CatchSlowQueriesExtension implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        TestMethodTracker.setCurrentTestMethod(context.getRequiredTestClass(), context.getRequiredTestMethod());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestMethodTracker.unsetCurrentTestMethod();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ExecutionPlanManager.verifyAll(context.getRequiredTestClass());
    }
}
