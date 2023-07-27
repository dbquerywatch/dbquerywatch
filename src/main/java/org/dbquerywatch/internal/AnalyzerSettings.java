package org.dbquerywatch.internal;

import java.util.List;

public interface AnalyzerSettings {

    List<String> getSmallTables();
    List<String> getAppBasePackages();
}
