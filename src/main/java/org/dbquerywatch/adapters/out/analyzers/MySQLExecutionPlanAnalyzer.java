package org.dbquerywatch.adapters.out.analyzers;

import com.jayway.jsonpath.JsonPath;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.SeqScan;
import org.dbquerywatch.application.port.out.AnalysisReport;
import org.dbquerywatch.application.port.out.JdbcClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toList;

public class MySQLExecutionPlanAnalyzer extends AbstractExecutionPlanAnalyzer {

    private static final String EXPLAIN_PLAN_QUERY = "EXPLAIN FORMAT=JSON ";

    private static final Pattern TABLE_ALIAS_DEFINITION = Pattern.compile(
        "\\b(?:from|join)\\s+([\\w.]+)\\s+([\\w.]+)\\b",
        CASE_INSENSITIVE
    );
    private static final JsonPath JSON_PATH = JsonPath.compile("$..table");

    public MySQLExecutionPlanAnalyzer(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public AnalysisReport analyze(String querySql, List<ParameterSetOperation> operations) {
        Map<String, String> tableAliases = findTableAliases(querySql);
        String planJson = jdbcClient.queryForString(EXPLAIN_PLAN_QUERY + querySql, operations)
            .orElseThrow(NoSuchElementException::new);
        List<Map<String, Object>> plan = JsonPath.parse(planJson).read(JSON_PATH);
        List<SeqScan> seqScans = plan.stream()
            .filter(p -> "ALL".equals(p.get("access_type")))
            .map(p -> {
                String tableName = getString(p, "table_name");
                String objectName = tableAliases.getOrDefault(tableName, tableName);
                String predicate = getString(p, "attached_condition");
                return objectName != null ? new SeqScan(objectName, predicate) : null;
            })
            .filter(Objects::nonNull)
            .collect(toList());
        return new AnalysisReport(compactJson(planJson), seqScans, 0);
    }

    private static Map<String, String> findTableAliases(String querySql) {
        Matcher matcher = TABLE_ALIAS_DEFINITION.matcher(querySql);
        Map<String, String> tableAliases = new HashMap<>();
        while (matcher.find()) {
            tableAliases.put(matcher.group(2), matcher.group(1));
        }
        return tableAliases;
    }
}
