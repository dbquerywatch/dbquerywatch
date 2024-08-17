package org.dbquerywatch.adapters.out.analyzers;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.SeqScan;
import org.dbquerywatch.application.port.out.AnalysisReport;
import org.dbquerywatch.application.port.out.JdbcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PostgresExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    public static final Logger LOGGER = LoggerFactory.getLogger(PostgresExecutionPlanAnalyzer.class);

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) ";
    private static final List<String> NODE_TYPES = singletonList("Seq Scan");

    private static final JsonPath SLOW_JSON_PATH;

    static {
        StringJoiner sj = new StringJoiner("','", "$..[?(@['Node Type'] in ['", "'])]");
        for (String nodeType : NODE_TYPES) {
            sj.add(nodeType);
        }
        SLOW_JSON_PATH = JsonPath.compile(sj.toString());
    }

    public PostgresExecutionPlanAnalyzer(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void checkConfiguration() {
        String state = jdbcClient.queryForString("SHOW ENABLE_SEQSCAN", emptyList())
            .orElse("");
        if (!"off".equalsIgnoreCase(state)) {
            LOGGER.warn("ENABLE_SEQSCAN is set to '{}' but expected 'off'", state);
        }
    }

    @Override
    public AnalysisReport analyze(String querySql, List<ParameterSetOperation> operations) {
        String planJson = jdbcClient.queryForString(EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        DocumentContext document = JsonPath.parse(planJson);
        List<SeqScan> seqScans = document
            .<List<Map<String, Object>>>read(SLOW_JSON_PATH).stream()
            .map(p -> {
                String objectName = getString(p, "Relation Name");
                String predicate = getString(p, "Filter");
                return objectName != null ? new SeqScan(objectName, predicate) : null;
            })
            .filter(Objects::nonNull)
            .collect(toList());
        return new AnalysisReport(compactJson(planJson), seqScans);
    }
}
