package com.parolisoft.dbquerywatch.internal;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.parolisoft.dbquerywatch.internal.JdbcTemplateUtils.queryForString;
import static java.util.Objects.requireNonNull;

class H2ExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN FOR ";
    private static final Pattern TABLE_SCAN_PATTERN = Pattern.compile("/\\* (.*)\\.tableScan \\*/");

    H2ExecutionPlanAnalyzer(String dataSourceName, AnalyzerSettings settings, JdbcTemplate jdbcTemplate) {
        super(dataSourceName, settings, jdbcTemplate);
    }

    @Override
    public List<Issue> analyze(String querySql, List<ParameterSetOperation> operations) {
        String commentedPlan = queryForString(jdbcTemplate, EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        Matcher matcher = TABLE_SCAN_PATTERN.matcher(requireNonNull(commentedPlan));
        List<Issue> issues = new ArrayList<>();
        while (matcher.find()) {
            issues.add(new Issue(IssueType.FULL_ACCESS, matcher.group(1), null));
        }
        return issues;
    }
}
