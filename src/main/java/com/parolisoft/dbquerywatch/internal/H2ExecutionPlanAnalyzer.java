package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

class H2ExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN FOR ";
    private static final Pattern TABLE_SCAN_PATTERN = Pattern.compile("/\\* (.*)\\.tableScan \\*/");

    private final JdbcTemplate jdbcTemplate;

    H2ExecutionPlanAnalyzer(String name, AnalyzerSettings settings, JdbcTemplate jdbcTemplate) {
        super(name, settings);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Issue> analyze(String querySql, List<ParameterSetOperation> operations) {
        String commentedPlan = jdbcTemplate.query(
            EXPLAIN_PLAN_QUERY + querySql,
            ps -> setParameters(ps, operations),
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
