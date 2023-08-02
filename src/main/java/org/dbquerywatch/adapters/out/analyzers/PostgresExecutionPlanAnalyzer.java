package org.dbquerywatch.adapters.out.analyzers;

import com.jayway.jsonpath.JsonPath;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.Issue;
import org.dbquerywatch.application.domain.model.IssueType;
import org.dbquerywatch.application.domain.service.ExecutionPlanAnalyzerException;
import org.dbquerywatch.application.port.out.AnalysisResult;
import org.dbquerywatch.application.port.out.JdbcClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class PostgresExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

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

    public PostgresExecutionPlanAnalyzer(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void checkConfiguration() {
        String state = jdbcClient.queryForString("SHOW ENABLE_SEQSCAN", emptyList())
            .orElse("");
        if (!"off".equalsIgnoreCase(state)) {
            throw new ExecutionPlanAnalyzerException(String.format("ENABLE_SEQSCAN is set to '%s' but expected 'off'", state));
        }
    }

    @Override
    public AnalysisResult analyze(String querySql, List<ParameterSetOperation> operations) {
        String planJson = jdbcClient.queryForString(EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        List<Map<String, Object>> plan = JsonPath.parse(planJson).read(JSON_PATH);
        List<Issue> issues = plan.stream()
            .map(p -> {
                String objectName = getString(p, "Relation Name");
                String predicate = getString(p, "Filter");
                return objectName != null ? new Issue(IssueType.FULL_ACCESS, objectName, predicate) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return new AnalysisResult(compactJson(planJson), issues);
    }
}
