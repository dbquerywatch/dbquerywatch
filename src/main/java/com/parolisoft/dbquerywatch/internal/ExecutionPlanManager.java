package com.parolisoft.dbquerywatch.internal;

import lombok.Value;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.SqlUtils.tableNameMatch;
import static com.parolisoft.dbquerywatch.internal.ClassHashSupport.classHashId;
import static java.util.Collections.emptyList;

@Slf4j
@ExtensionMethod({String.class, StringUtils.class})
public class ExecutionPlanManager {

    private static final Pattern ANALYZABLE_COMMANDS = Pattern.compile(
        "^\\s*(delete|insert|merge|replace|select|table|update|with)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Structured as:
    //
    // classHashId:
    //   analyzer:
    //     queryDsl:
    //      - operations
    //      - methods
    private static final Map<String, Map<ExecutionPlanAnalyzer, Map<String, QueryUsage>>> QUERIES = new ConcurrentHashMap<>();

    public static void afterQuery(
        ExecutionPlanAnalyzer analyzer,
        AnalyzerSettings settings,
        String querySql,
        List<List<ParameterSetOperation>> parameterSetOperations
    ) {
        if (!isAnalyzableStatement(querySql)) {
            return;
        }
        TraceMdcUtils.getTestClassHashId().ifPresent(classHashId -> {
            QueryUsage usages = QUERIES.computeIfAbsent(classHashId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(analyzer, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(querySql, k -> new QueryUsage());
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (usages) {
                usages.methods.add(findAppCallerMethod(settings.appBasePackages()));
                usages.allOperations.addAll(parameterSetOperations);
            }
        });
    }

    public static void verifyAll(AnalyzerSettings settings, Class<?> clazz) {
        Map<ExecutionPlanAnalyzer, Map<String, QueryUsage>> usagesPerAnalyzer = QUERIES.remove(classHashId(clazz));
        if (usagesPerAnalyzer == null) {
            return;
        }
        List<SlowQueryReport> slowQueries = new ArrayList<>();
        usagesPerAnalyzer.forEach((analyzer, usagesPerSql) ->
            usagesPerSql.forEach((querySql, usages) -> {
                AnalysisResult result = analyzer.analyze(querySql, firstOrElse(usages.allOperations, emptyList()));
                List<Issue> issues = result.getIssues().stream()
                    .filter(issue ->
                        settings.smallTables().stream()
                            .noneMatch(st -> tableNameMatch(st, issue.getObjectName()))
                    )
                    .collect(Collectors.toList());
                if (log.isDebugEnabled()) {
                    log.debug("Query SQL: {}", querySql);
                    log.debug("Execution plan: {}", result.getExecutionPlan());
                    log.debug("Issues: {}", issues);
                }
                if (!issues.isEmpty()) {
                    slowQueries.add(new SlowQueryReport(analyzer.getDataSourceName(), analyzer.getDataSource(),
                        querySql, result.getExecutionPlan(), usages.methods, issues));
                }
            })
        );
        if (!slowQueries.isEmpty()) {
            throw new SlowQueriesFoundException(slowQueries);
        }
    }

    private static String findAppCallerMethod(List<String> basePackages) {
        StackTraceElement[] stackTraceElements = new RuntimeException().getStackTrace();
        for (String basePackage : basePackages) {
            for (int i = stackTraceElements.length - 1; i >= 0; i--) {
                StackTraceElement st = stackTraceElements[i];
                if (st.getClassName().prefixedBy(basePackage, '.')) {
                    return st.getClassName() + "::" + st.getMethodName();
                }
            }
        }
        return "UNKNOWN";
    }

    private static boolean isAnalyzableStatement(String querySql) {
        return ANALYZABLE_COMMANDS.matcher(querySql).find();
    }

    private static <T> T firstOrElse(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }

    @Value
    private static class QueryUsage {
        // we are storing ALL sets of ParameterSetOperation although only the first set is used for the analysis.
        List<List<ParameterSetOperation>> allOperations = new ArrayList<>();
        Set<String> methods = new TreeSet<>();
    }
}
