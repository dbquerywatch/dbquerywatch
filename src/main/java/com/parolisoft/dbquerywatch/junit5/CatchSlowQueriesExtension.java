package com.parolisoft.dbquerywatch.junit5;

import com.jayway.jsonpath.JsonPath;
import com.parolisoft.dbquerywatch.NoQueriesWereAnalyzed;
import com.parolisoft.dbquerywatch.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import com.parolisoft.dbquerywatch.internal.ClassIdRepository;
import com.parolisoft.dbquerywatch.internal.ExecutionPlanManager;
import com.parolisoft.dbquerywatch.internal.spring.AnalyzerSettingsAdapter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CatchSlowQueriesExtension implements BeforeAllCallback, BeforeTestExecutionCallback, AfterEachCallback, AfterAllCallback {

    private static final String PROPERTY_SOURCE_NAME = "custom.configuration.parameters";

    @Override
    public void beforeAll(ExtensionContext context) {
        exportJUnitConfigurationParametersToSpringProperties(context);
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
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        AnalyzerSettings settings = new AnalyzerSettingsAdapter(springContext.getEnvironment());
        ExecutionPlanManager.verifyAll(settings, context.getRequiredTestClass());
    }

    private void exportJUnitConfigurationParametersToSpringProperties(ExtensionContext context) {
        List<String> propertyNames = getPropertyNames();
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        Map<String, Object> parameters = getConfigurationParameters(context, propertyNames);
        MutablePropertySources propertySources = ((ConfigurableEnvironment) springContext.getEnvironment()).getPropertySources();
        PropertySource<?> propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, parameters);
        propertySources.addLast(propertySource);  // or replace it
    }

    @SneakyThrows
    private List<String> getPropertyNames() {
        try (InputStream resource = getClass().getResourceAsStream("/META-INF/additional-spring-configuration-metadata.json")) {
            return JsonPath.parse(resource).read("$.properties[*].name");
        }
    }

    private static Map<String, Object> getConfigurationParameters(ExtensionContext context, Iterable<String> names) {
        Map<String, Object> parameters = new HashMap<>();
        for (String name : names) {
            context.getConfigurationParameter(name)
                .ifPresent(value -> parameters.put(name, value));
        }
        return parameters;
    }
}
