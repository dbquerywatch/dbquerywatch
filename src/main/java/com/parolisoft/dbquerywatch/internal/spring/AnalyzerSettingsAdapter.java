package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class AnalyzerSettingsAdapter implements AnalyzerSettings {

    private final Environment environment;

    private final Map<String, String> rawValues = new ConcurrentHashMap<>();
    private final Map<String, List<String>> cacheValues = new ConcurrentHashMap<>();

    @Override
    public List<String> smallTables() {
        return getPropertyList("dbquerywatch.small-tables");
    }

    @Override
    public List<String> appBasePackages() {
        return getPropertyList("dbquerywatch.app-base-packages");
    }

    private List<String> getPropertyList(String key) {
        String newRawValue = environment.getProperty(key, "");
        String oldRawValue = rawValues.put(key, newRawValue);
        if (!newRawValue.equals(oldRawValue)) {
            List<String> value = newRawValue.isEmpty()
                ? emptyList()
                : Collections.unmodifiableList(Arrays.asList(newRawValue.split(",")));
            cacheValues.put(key, value);
        }
        return cacheValues.getOrDefault(key, emptyList());
    }
}
