package com.parolisoft.dbquerywatch.internal;

import lombok.Value;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

@Value
public class AnalyzerSettings {

    List<String> smallTables;

    public static AnalyzerSettings from(Environment environment) {
        List<String> smallTables = getPropertyList(environment, "dbquerywatch.small-tables");
        smallTables.replaceAll(String::toLowerCase);
        return new AnalyzerSettings(smallTables);
    }

    private static List<String> getPropertyList(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null) {
            return emptyList();
        }
        return Arrays.asList(value.split(","));
    }
}
