package org.dbquerywatch.adapters.out.analyzers;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.SeqScan;
import org.dbquerywatch.application.port.out.AnalysisReport;
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
    public AnalysisReport analyze(String querySql, List<ParameterSetOperation> operations) {
        String commentedPlan = jdbcClient.queryForString(EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        Matcher matcher = TABLE_SCAN_PATTERN.matcher(requireNonNull(commentedPlan));
        List<SeqScan> seqScans = new ArrayList<>();
        while (matcher.find()) {
            seqScans.add(new SeqScan(matcher.group(1), null));
        }
        return new AnalysisReport(stripLineFeeds(commentedPlan), seqScans, 0);
    }

    private static String stripLineFeeds(String text) {
        return text.replaceAll(" *\r?\n *", " ");
    }
}
