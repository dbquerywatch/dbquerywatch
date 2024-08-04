package org.dbquerywatch.api.spring.junit5;

import org.dbquerywatch.application.domain.service.AnalyzerSettings;
import org.dbquerywatch.application.domain.service.ClassIdRepository;
import org.dbquerywatch.application.domain.service.ExecutionPlanManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.Objects.requireNonNull;

class CatchSlowQueriesExtension implements BeforeAllCallback, BeforeTestExecutionCallback, AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        GenericApplicationContext springContext = (GenericApplicationContext) SpringExtension.getApplicationContext(context);
        AnalyzerSettings settings = springContext.getBean(AnalyzerSettings.class);
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
    public void afterAll(ExtensionContext context) {
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        ExecutionPlanManager executionPlanManager = requireNonNull(springContext.getBean(ExecutionPlanManager.class));
        executionPlanManager.verifyAll(context.getRequiredTestClass());
    }
}
