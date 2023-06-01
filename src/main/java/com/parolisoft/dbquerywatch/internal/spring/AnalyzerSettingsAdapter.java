package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class AnalyzerSettingsAdapter implements AnalyzerSettings {

    private final Environment environment;

    private final Map<String, String> rawValues = new ConcurrentHashMap<>();
    private final Map<String, List<String>> cacheValues = new ConcurrentHashMap<>();

    @Override
    public List<String> smallTables() {
        return getPropertyList("dbquerywatch.small-tables", Collections::emptyList);
    }

    @Override
    public List<String> appBasePackages() {
        return getPropertyList("dbquerywatch.app-base-packages", SpringBootApplicationRunListener::getBasePackages);
    }

    private List<String> getPropertyList(String key, Supplier<List<String>> defaultValue) {
        String newRawValue = environment.getProperty(key, "");
        String oldRawValue = rawValues.put(key, newRawValue);
        if (newRawValue.equals(oldRawValue)) {
            return cacheValues.getOrDefault(key, defaultValue.get());
        }
        List<String> value = newRawValue.isEmpty()
            ? defaultValue.get()
            : Collections.unmodifiableList(Arrays.asList(newRawValue.split(",")));
        cacheValues.put(key, value);
        return value;
    }
}
