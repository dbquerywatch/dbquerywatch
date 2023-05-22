package com.parolisoft.dbquerywatch.junit5;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import com.parolisoft.dbquerywatch.internal.ExecutionPlanManager;
import com.parolisoft.dbquerywatch.internal.TraceMdcUtils;
import com.parolisoft.dbquerywatch.internal.spring.AnalyzerSettingsAdapter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
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

import static com.parolisoft.dbquerywatch.internal.JsonPathUtils.JSON_PATH_CONFIGURATION;

@Slf4j
class CatchSlowQueriesExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String PROPERTY_SOURCE_NAME = "custom.configuration.parameters";

    @Override
    public void beforeAll(ExtensionContext context) {
        List<String> propertyNames = getPropertyNames();
        Map<String, Object> parameters = getConfigurationParameters(context, propertyNames);
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        MutablePropertySources propertySources = ((ConfigurableEnvironment) springContext.getEnvironment()).getPropertySources();
        PropertySource<?> propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, parameters);
        propertySources.addLast(propertySource);  // or replace it

        TraceMdcUtils.setTestClass(context.getRequiredTestClass());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        AnalyzerSettings settings = new AnalyzerSettingsAdapter(springContext.getEnvironment());
        ExecutionPlanManager.verifyAll(settings, context.getRequiredTestClass());

        TraceMdcUtils.clearTestClass();
    }

    @SneakyThrows
    private List<String> getPropertyNames() {
        try (InputStream resource = getClass().getResourceAsStream("/META-INF/additional-spring-configuration-metadata.json")) {
            return JsonPath.parse(resource, JSON_PATH_CONFIGURATION)
                .read("$.properties[*].name", new TypeRef<List<String>>() {});
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
