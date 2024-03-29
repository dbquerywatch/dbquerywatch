package org.dbquerywatch.configuration.spring;

import lombok.Getter;
import org.dbquerywatch.application.domain.service.AnalyzerSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "dbquerywatch")
@Getter(onMethod_ = {@Override})
@SuppressWarnings("FieldMayBeFinal")
class AnalyzerProperties implements AnalyzerSettings {
    private List<String> smallTables = new ArrayList<>();
    private List<String> appBasePackages = new ArrayList<>();
}
