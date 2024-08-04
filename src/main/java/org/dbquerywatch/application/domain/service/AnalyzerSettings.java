package org.dbquerywatch.application.domain.service;

import java.util.List;

public interface AnalyzerSettings {
    List<String> getSmallTables();

    List<String> getAppBasePackages();
}
