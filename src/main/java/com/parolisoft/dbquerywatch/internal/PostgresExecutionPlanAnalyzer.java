package com.parolisoft.dbquerywatch.internal;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.JdbcTemplateUtils.queryForString;
import static com.parolisoft.dbquerywatch.internal.JsonPathUtils.JSON_PATH_CONFIGURATION;

class PostgresExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN (FORMAT JSON) ";
    private static final List<String> NODE_TYPES = Collections.singletonList("Seq Scan");

    private static final JsonPath JSON_PATH;

    static {
        StringJoiner sj = new StringJoiner("','", "$..[?(@['Node Type'] in ['", "'])]");
        for (String nodeType : NODE_TYPES) {
            sj.add(nodeType);
        }
        JSON_PATH = JsonPath.compile(sj.toString());
    }

    PostgresExecutionPlanAnalyzer(String dataSourceName, AnalyzerSettings settings, JdbcTemplate jdbcTemplate) {
        super(dataSourceName, settings, jdbcTemplate);
    }

    @Override
    public AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations) {
        String planJson = queryForString(jdbcTemplate, EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        List<Map<String, Object>> plan = JsonPath
            .parse(planJson, JSON_PATH_CONFIGURATION)
            .read(JSON_PATH, new TypeRef<List<Map<String, Object>>>() {});
        List<Issue> issues = plan.stream()
            .map(p -> {
                String objectName = getString(p, "Relation Name");
                String predicate = getString(p, "Filter");
                return new Issue(IssueType.FULL_ACCESS, objectName, predicate);
            })
            .collect(Collectors.toList());
        return new AnalysisResult(compactJson(planJson), issues);
    }
}
