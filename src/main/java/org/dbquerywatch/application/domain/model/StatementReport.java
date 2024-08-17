package org.dbquerywatch.application.domain.model;

import org.dbquerywatch.common.Pojo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public interface StatementReport extends Pojo {
    NamedDataSource getNamedDataSource();

    String getSqlStatement();

    String getExecutionPlan();

    Set<String> getMethods();

    @Override
    default Map<String, Object> toPojo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("DataSource", format("'%s (%s)'", getNamedDataSource().getName(), getNamedDataSource().getProductName()));
        result.put("SQL", format("\"%s\"", getSqlStatement()));
        result.put("ExecutionPlan", format("'%s'", getExecutionPlan()));
        result.put("CallerMethods", getMethods());
        return result;
    }
}
