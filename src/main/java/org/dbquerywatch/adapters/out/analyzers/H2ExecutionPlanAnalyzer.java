package org.dbquerywatch.adapters.out.analyzers;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.Issue;
import org.dbquerywatch.application.domain.model.IssueType;
import org.dbquerywatch.application.port.out.AnalysisResult;
import org.dbquerywatch.application.port.out.JdbcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class H2ExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN PLAN FOR ";
    private static final Pattern TABLE_SCAN_PATTERN = Pattern.compile("/\\* (.*)\\.tableScan \\*/");

    public H2ExecutionPlanAnalyzer(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations) {
        String commentedPlan = jdbcClient.queryForString(EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        Matcher matcher = TABLE_SCAN_PATTERN.matcher(requireNonNull(commentedPlan));
        List<Issue> issues = new ArrayList<>();
        while (matcher.find()) {
            issues.add(new Issue(IssueType.FULL_ACCESS, matcher.group(1), null));
        }
        return new AnalysisResult(commentedPlan, issues);
    }
}
