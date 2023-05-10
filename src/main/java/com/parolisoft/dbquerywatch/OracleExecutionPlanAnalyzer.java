package com.parolisoft.dbquerywatch;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class OracleExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN SET STATEMENT_ID = '%s' FOR %s";
    private static final String GET_PLAN_QUERY = "SELECT * FROM plan_table WHERE statement_id = ?";
    private static final List<String> OPERATIONS = Arrays.asList("INDEX", "MAT_VIEW REWRITE ACCESS", "TABLE ACCESS");
    private static final List<String> OPTIONS = Arrays.asList("FULL SCAN", "FULL SCAN DESCENDING", "FULL");

    private final JdbcTemplate jdbcTemplate;

    public List<Issue> analyze(String statementId, String querySql, Object[] parameters) {
        String explainPlanSql = String.format(EXPLAIN_PLAN_QUERY, statementId, querySql);
        jdbcTemplate.query(
                explainPlanSql,
                new ArgumentPreparedStatementSetter(parameters),
                (rs, rowNum) -> null
        );
        List<Map<String, Object>> plans = jdbcTemplate.queryForList(GET_PLAN_QUERY, statementId);
        return plans.stream()
                .filter(plan -> OPERATIONS.contains(String.valueOf(plan.get("OPERATION"))) &&
                        OPTIONS.contains(String.valueOf(plan.get("OPTIONS"))))
                .map(plan -> {
                    String objectName = String.valueOf(plan.get("OBJECT_NAME"));
                    String filter = String.valueOf(plan.get("FILTER_PREDICATES"));
                    return new Issue(IssueType.FULL_ACCESS, objectName, filter);
                })
                .collect(Collectors.toList());
    }
}
