package com.parolisoft.dbquerywatch.internal;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.JdbcTemplateUtils.queryForString;
import static com.parolisoft.dbquerywatch.internal.JsonPathUtils.JSON_PATH_CONFIGURATION;

class MySQLExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN FORMAT=JSON ";

    private static final Pattern TABLE_ALIAS_DEFINITION = Pattern.compile(
        "\\bfrom\\s+([\\w.]+)\\s+([\\w.]+)\\b",
        Pattern.CASE_INSENSITIVE
    );
    private static final JsonPath JSON_PATH = JsonPath.compile("$..table");

    MySQLExecutionPlanAnalyzer(String dataSourceName, AnalyzerSettings settings, JdbcTemplate jdbcTemplate) {
        super(dataSourceName, settings, jdbcTemplate);
    }

    @Override
    public AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations) {
        Map<String, String> tableAliases = findTableAliases(querySql);
        String planJson = queryForString(jdbcTemplate, EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        List<Map<String, Object>> plan = JsonPath
            .parse(planJson, JSON_PATH_CONFIGURATION)
            .read(JSON_PATH, new TypeRef<List<Map<String, Object>>>() {
            });
        List<Issue> issues = plan.stream()
            .filter(p -> "ALL".equals(p.get("access_type")))
            .map(p -> {
                String tableName = getString(p, "table_name");
                String objectName = tableAliases.getOrDefault(tableName, tableName);
                String predicate = getString(p, "attached_condition");
                return new Issue(IssueType.FULL_ACCESS, objectName, predicate);
            })
            .collect(Collectors.toList());
        return new AnalysisResult(compactJson(planJson), issues);
    }

    @Nonnull
    private static Map<String, String> findTableAliases(String querySql) {
        Matcher matcher = TABLE_ALIAS_DEFINITION.matcher(querySql);
        Map<String, String> tableAliases = new HashMap<>();
        while (matcher.find()) {
            tableAliases.put(matcher.group(2), matcher.group(1));
        }
        return tableAliases;
    }
}
