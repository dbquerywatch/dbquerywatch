package com.parolisoft.dbquerywatch.internal;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

@Data
@Builder
class AnalyzerSettings {

    @Builder.Default
    List<String> smallTables = new ArrayList<>();

    public static AnalyzerSettings from(Environment environment) {
        AnalyzerSettings.AnalyzerSettingsBuilder builder = AnalyzerSettings.builder();
        List<String> smallTables = getPropertyList(environment, "dbquerywatch.small-tables");
        smallTables.replaceAll(String::toLowerCase);
        builder.smallTables(smallTables);
        return builder.build();
    }

    private static List<String> getPropertyList(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null) {
            return emptyList();
        }
        return Arrays.asList(value.split(","));
    }
}
