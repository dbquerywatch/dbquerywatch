package com.parolisoft.dbquerywatch.junit5;

import com.parolisoft.dbquerywatch.NoQueriesWereAnalyzed;
import com.parolisoft.dbquerywatch.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import com.parolisoft.dbquerywatch.internal.ClassIdRepository;
import com.parolisoft.dbquerywatch.internal.ExecutionPlanManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class CatchSlowQueriesExtension implements BeforeTestExecutionCallback, AfterEachCallback, AfterAllCallback {

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
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        AnalyzerSettings settings = springContext.getBean(AnalyzerSettings.class);
        ExecutionPlanManager.verifyAll(settings, context.getRequiredTestClass());
    }
}
