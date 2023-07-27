package org.dbquerywatch.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dbquerywatch.internal.jdbc.JdbcClient;
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

    // Support Methods
    // ------------------------------------------------------------------------

    @Nullable
    protected static <K, V> String getString(Map<K, V> map, K key) {
        V value = map.get(key);
        return value != null ? value.toString() : null;
    }

    protected static String toJson(Collection<?> array) {
        return new JSONArray(array).toString();
    }

    protected static String compactJson(String prettyJson) {
        if (IS_JSON_ARRAY.matcher(prettyJson).find()) {
            return new JSONArray(prettyJson).toString();
        }
        return new JSONObject(prettyJson).toString();
    }
}
