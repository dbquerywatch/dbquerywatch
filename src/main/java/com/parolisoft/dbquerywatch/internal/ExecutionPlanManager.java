package com.parolisoft.dbquerywatch.internal;

import com.parolisoft.dbquerywatch.NoQueriesWereAnalyzed;
import com.parolisoft.dbquerywatch.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.Value;
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

import static com.parolisoft.dbquerywatch.internal.ClassIdSupport.generateClassId;
import static com.parolisoft.dbquerywatch.internal.SqlUtils.tableNameMatch;
import static com.parolisoft.dbquerywatch.internal.Strings.prefixedBy;
import static java.util.Collections.emptyList;

@Slf4j
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
        ClassIdRepository.load().ifPresent(classHashId -> {
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

    public static void verifyAll(AnalyzerSettings settings, Class<?> clazz) throws SlowQueriesFoundException, NoQueriesWereAnalyzed {
        Map<ExecutionPlanAnalyzer, Map<String, QueryUsage>> usagesPerAnalyzer = QUERIES.remove(generateClassId(clazz));
        if (usagesPerAnalyzer == null) {
            throw new NoQueriesWereAnalyzed();
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
                    JdbcClient jdbcClient = analyzer.getJdbcClient();
                    slowQueries.add(new SlowQueryReport(jdbcClient.getDataSourceName(), jdbcClient.getDataSource(),
                        querySql, result.getExecutionPlan(), usages.methods, issues));
                }
            })
        );
        if (!slowQueries.isEmpty()) {
            throw new SlowQueriesFoundException(slowQueries);
        }
    }

    private static final String LIB_PACKAGE = ExecutionPlanManager.class.getPackage().getName();

    private static String findAppCallerMethod(List<String> basePackages) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (String basePackage : basePackages) {
            for (StackTraceElement st : stackTraceElements) {
                if (isSyntheticClass(st.getClassName())) {
                    continue;
                }
                if (prefixedBy(st.getClassName(), LIB_PACKAGE, false, '.')) {
                    continue;
                }
                if (prefixedBy(st.getClassName(), basePackage, false, '.')) {
                    return st.getClassName() + "::" + st.getMethodName();
                }
            }
        }
        return "UNKNOWN";
    }

    private static boolean isSyntheticClass(String className) {
        // TODO: cover other cases: anonymous, lambda function, ... Validate with Unit Tests
        return className.contains(".$Proxy");
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
