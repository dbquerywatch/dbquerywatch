package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.JdbcTemplateUtils.queryForString;

class OracleExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN SET STATEMENT_ID = '%s' FOR %s";
    private static final String GET_PLAN_QUERY = "SELECT * FROM plan_table WHERE statement_id = ?";
    private static final List<String> OPERATIONS = Arrays.asList("INDEX", "MAT_VIEW REWRITE ACCESS", "TABLE ACCESS");
    private static final List<String> OPTIONS = Arrays.asList("FULL SCAN", "FULL SCAN DESCENDING", "FULL");

    OracleExecutionPlanAnalyzer(String dataSourceName, AnalyzerSettings settings, JdbcTemplate jdbcTemplate) {
        super(dataSourceName, settings, jdbcTemplate);
    }

    public AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations) {
        String statementId = getStatementID();
        String explainPlanSql = String.format(EXPLAIN_PLAN_QUERY, statementId, querySql);
        queryForString(jdbcTemplate, explainPlanSql, operations);
        List<Map<String, Object>> plans = jdbcTemplate.queryForList(GET_PLAN_QUERY, statementId);
        removeNoisyProperties(plans);
        List<Issue> issues = plans.stream()
                .filter(plan -> OPERATIONS.contains(getString(plan, "OPERATION")) &&
                        OPTIONS.contains(getString(plan, "OPTIONS")))
                .map(plan -> {
                    String objectName = getString(plan, "OBJECT_NAME");
                    String predicate = getString(plan, "FILTER_PREDICATES");
                    return new Issue(IssueType.FULL_ACCESS, objectName, predicate);
                })
                .collect(Collectors.toList());
        return new AnalysisResult(toJson(plans), issues);
    }

    private static String getStatementID() {
        String uuid = UUID.randomUUID().toString();
        // Oracle STATEMENT_ID capacity is 30 chars.
        return uuid.substring(uuid.length() - (24 + 3));
    }

    private static void removeNoisyProperties(List<Map<String, Object>> plans) {
        for (Map<String, Object> plan : plans) {
            plan.remove("OTHER_XML");
        }
    }
}
