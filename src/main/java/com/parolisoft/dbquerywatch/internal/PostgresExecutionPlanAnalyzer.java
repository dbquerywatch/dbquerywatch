package com.parolisoft.dbquerywatch.internal;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.JsonPathUtils.JSON_PATH_CONFIGURATION;

@RequiredArgsConstructor
class PostgresExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

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

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Issue> analyze(String statementId, String querySql, Object[] args) {
        String planJson = jdbcTemplate.query(
                EXPLAIN_PLAN_QUERY + querySql,
                new ArgumentPreparedStatementSetter(args),
                rs -> {
                    rs.next();
                    return rs.getString(1);
                });
        List<Map<String, Object>> plan = JsonPath
                .parse(planJson, JSON_PATH_CONFIGURATION)
                .read(JSON_PATH, new TypeRef<List<Map<String, Object>>>() {});
        return plan.stream()
                .map(p -> {
                    String objectName = String.valueOf(p.get("Relation Name"));
                    String filter = String.valueOf(p.get("Filter"));
                    return new Issue(IssueType.FULL_ACCESS, objectName, filter);
                })
                .collect(Collectors.toList());
    }
}
