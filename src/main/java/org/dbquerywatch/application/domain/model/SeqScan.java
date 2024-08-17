package org.dbquerywatch.application.domain.model;

import org.dbquerywatch.common.Pojo;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.String.format;

public class SeqScan implements Pojo, Serializable {

    private final String objectName;
    @Nullable
    private final String predicate;

    public SeqScan(String objectName, @Nullable String predicate) {
        this.objectName = objectName;
        this.predicate = predicate;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public Map<String, Object> toPojo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", objectName);
        if (predicate != null) {
            result.put("predicate", format("\"%s\"", predicate));
        }
        return result;
    }
}
