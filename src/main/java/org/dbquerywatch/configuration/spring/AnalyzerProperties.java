package org.dbquerywatch.configuration.spring;

import org.dbquerywatch.application.domain.service.AnalyzerSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "dbquerywatch")
@SuppressWarnings("FieldMayBeFinal")
class AnalyzerProperties implements AnalyzerSettings {
    private List<String> smallTables = new ArrayList<>();
    private List<String> appBasePackages = new ArrayList<>();

    @Override
    public List<String> getSmallTables() {
        return this.smallTables;
    }

    @Override
    public List<String> getAppBasePackages() {
        return this.appBasePackages;
    }
}
