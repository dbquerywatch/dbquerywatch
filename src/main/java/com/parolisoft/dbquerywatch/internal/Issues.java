package com.parolisoft.dbquerywatch.internal;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
class Issues {

    private static final JsonMapper jsonMapper = new JsonMapper();

    @SneakyThrows
    public String toString(List<Issue> issues) {
        return jsonMapper.writeValueAsString(issues);
    }

    @SneakyThrows
    public List<Issue> fromString(String json) {
        return jsonMapper.readerForListOf(Issue.class).readValue(json);
    }
}
