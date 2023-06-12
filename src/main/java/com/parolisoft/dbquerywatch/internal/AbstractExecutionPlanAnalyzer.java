package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
abstract class AbstractExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    private static final Pattern IS_JSON_ARRAY = Pattern.compile("^\\s*\\[");

    protected final JdbcClient jdbcClient;

    @Override
    public JdbcClient getJdbcClient() {
        return jdbcClient;
    }

    @Override
    public void checkConfiguration() {
        // everything is okay by default
    }

    // TODO: move to Maps.getAsString
    @Nullable
    protected static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    // TODO: move to Json.toJson
    protected static String toJson(Collection<?> array) {
        return new JSONArray(array).toString();
    }

    // TODO: move to Json.compactJson
    protected static String compactJson(String prettyJson) {
        if (IS_JSON_ARRAY.matcher(prettyJson).find()) {
            return new JSONArray(prettyJson).toString();
        }
        return new JSONObject(prettyJson).toString();
    }
}
