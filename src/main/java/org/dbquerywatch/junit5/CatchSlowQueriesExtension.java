package org.dbquerywatch.junit5;

import lombok.val;
import org.dbquerywatch.NoQueriesWereAnalyzed;
import org.dbquerywatch.SlowQueriesFoundException;
import org.dbquerywatch.internal.AnalyzerSettings;
import org.dbquerywatch.internal.ClassIdRepository;
import org.dbquerywatch.internal.ExecutionPlanManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Objects.requireNonNull;

class CatchSlowQueriesExtension implements BeforeAllCallback, BeforeTestExecutionCallback, AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        val springContext = (GenericApplicationContext) SpringExtension.getApplicationContext(context);
        val settings = springContext.getBean(AnalyzerSettings.class);
        springContext.registerBean(ExecutionPlanManager.class, settings);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ClassIdRepository.save(context.getRequiredTestClass());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ClassIdRepository.clear();
    }

    @Override
    public void afterAll(ExtensionContext context) throws SlowQueriesFoundException, NoQueriesWereAnalyzed {
        val springContext = SpringExtension.getApplicationContext(context);
        val executionPlanManager = requireNonNull(springContext.getBean(ExecutionPlanManager.class));
        executionPlanManager.verifyAll(context.getRequiredTestClass());
    }
}
