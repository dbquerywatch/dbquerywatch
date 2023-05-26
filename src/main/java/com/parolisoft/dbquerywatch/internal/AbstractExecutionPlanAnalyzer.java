package com.parolisoft.dbquerywatch.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"dataSourceName"})
@ToString(of = {"dataSourceName"})
@Getter
abstract class AbstractExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    private static final Pattern IS_JSON_ARRAY = Pattern.compile("^\\s*\\[");

    @Nonnull
    private final String dataSourceName;

    @Nonnull
    protected final JdbcTemplate jdbcTemplate;

    public DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

    @Nullable
    protected static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
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
