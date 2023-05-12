package com.parolisoft.dbquerywatch;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
class H2ExecutionPlanAnalyzer implements ExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN FOR ";
    private static final Pattern TABLE_SCAN_PATTERN = Pattern.compile("/\\* (.*)\\.tableScan \\*/");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Issue> analyze(String statementId, String querySql, Object[] args) {
        String commentedPlan = jdbcTemplate.query(
                EXPLAIN_PLAN_QUERY + querySql,
                new ArgumentPreparedStatementSetter(args),
                rs -> {
                    rs.next();
                    return rs.getString(1);
                });
        Matcher matcher = TABLE_SCAN_PATTERN.matcher(requireNonNull(commentedPlan));
        List<Issue> issues = new ArrayList<>();
        while (matcher.find()) {
            issues.add(new Issue(IssueType.FULL_ACCESS, matcher.group(1), null));
        }
        return issues;
    }
}
