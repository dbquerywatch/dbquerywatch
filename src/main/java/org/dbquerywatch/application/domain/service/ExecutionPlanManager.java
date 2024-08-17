package org.dbquerywatch.application.domain.service;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.dbquerywatch.application.domain.model.ImmutablePerStatementIssuesReport;
import org.dbquerywatch.application.domain.model.ReportElement;
import org.dbquerywatch.application.domain.model.SeqScan;
import org.dbquerywatch.application.domain.model.SimpleStatementReport;
import org.dbquerywatch.application.domain.model.StatementReport;
import org.dbquerywatch.application.port.out.AnalysisReport;
import org.dbquerywatch.application.port.out.ExecutionPlanAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toList;
import static org.dbquerywatch.common.SqlUtils.tableNameMatch;
import static org.dbquerywatch.common.Strings.prefixedBy;

/**
 * The {@code ExecutionPlanManager} class is responsible for managing the execution plans of SQL queries
 * and analyzing their performance.
 *
 * <p>
 * The class provides methods for accepting SQL queries, verifying their execution plans, and generating
 * reports on slow queries.
 *
 */
public class ExecutionPlanManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionPlanManager.class);

    private static final Pattern ANALYZABLE_STATEMENTS = Pattern.compile(
        "^\\s*(delete|insert|merge|replace|select|table|update|with)\\b",
        CASE_INSENSITIVE
    );

    // Structured as:
    //
    // testMethodHashId:
    //   analyzer:
    //     queryDsl:
    //      - operations
    //      - methods
    private static final Map<String, Map<ExecutionPlanAnalyzer, Map<String, StatementUsage>>> STATEMENTS = new ConcurrentHashMap<>();

    private final AnalyzerSettings settings;

    public ExecutionPlanManager(AnalyzerSettings settings) {
        this.settings = settings;
    }

    public void acceptSqlStatement(
        ExecutionPlanAnalyzer analyzer,
        String sqlStatement,
        List<List<ParameterSetOperation>> parameterSetOperations
    ) {
        if (!isAnalyzableStatement(sqlStatement)) {
            return;
        }
        TestMethodIdRepository.load().ifPresent(testMethodHashId -> {
            StatementUsage usages = STATEMENTS.computeIfAbsent(testMethodHashId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(analyzer, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(sqlStatement, k -> new StatementUsage());
            synchronized (usages) {
                usages.methods.add(findAppCallerMethod(settings.getAppBasePackages()));
                usages.allOperations.addAll(parameterSetOperations);
            }
        });
    }

    public void verifyAll(String uniqueId, Limits limits) throws DatabasePerformanceIssuesDetectedException {
        Map<ExecutionPlanAnalyzer, Map<String, StatementUsage>> usagesPerAnalyzer = STATEMENTS.remove(TestMethodIdSupport.generateTestMethodId(uniqueId));
        if (usagesPerAnalyzer == null) {
            LOGGER.warn("No query data found for {}", uniqueId);
            return;
        }
        List<ReportElement> reportElements = new ArrayList<>();
        usagesPerAnalyzer.forEach((analyzer, usagesPerSql) -> {
                analyzer.checkConfiguration();
                usagesPerSql.forEach((sqlStatement, usages) -> {
                    AnalysisReport analysisReport = analyzer.analyze(sqlStatement, firstOrElse(usages.allOperations, emptyList()));
                    StatementReport statementReport = new SimpleStatementReport(
                        analyzer.getJdbcClient().getNamedDataSource(),
                        sqlStatement,
                        analysisReport.getExecutionPlan(),
                        usages.methods
                    );
                    List<SeqScan> seqScans = analysisReport.getSeqScans().stream()
                        .filter(seqScan ->
                            settings.getSmallTables().stream()
                                .noneMatch(st -> tableNameMatch(st, seqScan.getObjectName()))
                        )
                        .collect(toList());
                    LOGGER.info("Query SQL: {}", sqlStatement);
                    LOGGER.info("Execution plan: {}", analysisReport.getExecutionPlan());
                    LOGGER.info("Seq Scans: {}", seqScans);
                    if (!seqScans.isEmpty() && !limits.allowSeqScans()) {
                        reportElements.add(ImmutablePerStatementIssuesReport.builder()
                            .from(statementReport)
                            .seqScans(seqScans)
                            .build());
                    }
                });
            }
        );
        if (!reportElements.isEmpty()) {
            throw new DatabasePerformanceIssuesDetectedException(reportElements);
        }
    }

    private static final String LIB_PACKAGE = ExecutionPlanManager.class.getPackage().getName();

    private static String findAppCallerMethod(List<String> basePackages) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (String basePackage : basePackages) {
            for (StackTraceElement st : stackTraceElements) {
                if (prefixedBy(st.getClassName(), basePackage, false, '.')
                    && !prefixedBy(st.getClassName(), LIB_PACKAGE, false, '.')
                    && !isSynthetic(st.getClassName(), st.getMethodName())) {
                    return st.getClassName() + "::" + st.getMethodName();
                }
            }
        }
        return "UNKNOWN";
    }

    private static final Pattern ANONYMOUS_CLASS_SUFFIX = Pattern.compile("\\$\\d+$");

    private static boolean isSynthetic(String className, String methodName) {
        return className.contains(".$Proxy")
            || methodName.startsWith("lambda$")
            || ANONYMOUS_CLASS_SUFFIX.matcher(className).find();
    }

    private static boolean isAnalyzableStatement(String querySql) {
        return ANALYZABLE_STATEMENTS.matcher(querySql).find();
    }

    private static <T> T firstOrElse(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }

    private static class StatementUsage {
        // we are storing ALL sets of ParameterSetOperation although only the first set is used for the analysis.
        List<List<ParameterSetOperation>> allOperations = new ArrayList<>();
        Set<String> methods = new TreeSet<>();
    }
}
