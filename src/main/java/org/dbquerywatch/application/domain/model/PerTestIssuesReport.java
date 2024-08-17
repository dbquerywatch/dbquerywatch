package org.dbquerywatch.application.domain.model;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Value.Immutable
public interface PerTestIssuesReport extends ReportElement, Serializable {
    long actual();

    long maximum();

    List<StatementReport> criticalStatements();

    @Override
    default Map<String, Object> toPojo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("Actual", actual());
        result.put("Maximum", maximum());
        if (!criticalStatements().isEmpty()) {
            result.put("CriticalStatements", criticalStatements());
        }
        return result;
    }
}
