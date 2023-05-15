package com.parolisoft.dbquerywatch.internal;

import lombok.Data;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ExecutionPlanManager {

    private static final Pattern ANALYZABLE_COMMANDS = Pattern.compile(
        "^\\s*(delete|insert|merge|replace|select|table|update|with)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Map<String, Map<ExecutionPlanAnalyzer, Map<String, QueryUsages>>> USAGES_PER_CLASS = new ConcurrentHashMap<>();

    public static void afterQuery(
        ExecutionPlanAnalyzer analyzer,
        String querySql,
        List<List<ParameterSetOperation>> parameterSetOperations
    ) {
        if (!isAnalyzableStatement(querySql)) {
            return;
        }
        TestMethodTracker.getCurrentTestMethod().ifPresent(testMethod -> {
            QueryUsages usages = USAGES_PER_CLASS.computeIfAbsent(testMethod.getClassName(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(analyzer, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(querySql, k -> new QueryUsages());
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (usages) {
                usages.methods.add(testMethod.getMethodName());
                usages.operations.addAll(parameterSetOperations);
            }
        });
    }

    public static void verifyAll(Class<?> clazz) {
        Map<ExecutionPlanAnalyzer, Map<String, QueryUsages>> usagesPerAnalyzer = USAGES_PER_CLASS.remove(clazz.getCanonicalName());
        assertNotNull(usagesPerAnalyzer);
        usagesPerAnalyzer.forEach((analyzer, usagesPerQuery) ->
            assertAll(
                "Potential slow queries were found",
                usagesPerQuery.entrySet().stream().map(entry -> {
                    String querySql = entry.getKey();
                    QueryUsages usages = entry.getValue();
                    List<Issue> issues = analyzer.analyze(querySql, firstOrElse(usages.operations, emptyList()));
                        issues.removeIf(issue ->
                            analyzer.getSettings().smallTables.stream()
                                .anyMatch(st -> tableNameMatch(st, issue.getObjectName()))
                        );
                        return () -> {
                            if (!issues.isEmpty()) {
                                String itemize = "\n    - ";
                                fail(String.format("\nQuery: %s\nIssues: %s\nTest methods:%s%s\n", querySql, issues,
                                    itemize, String.join(itemize, usages.methods)));
                            }
                        };
                    }
                )
            ));
    }

    private static boolean isAnalyzableStatement(String querySql) {
        return ANALYZABLE_COMMANDS.matcher(querySql).find();
    }

    private static boolean tableNameMatch(String targetName, String issueName) {
        int issueNameLen = issueName.length();
        int targetNameLen = targetName.length();
        String canonicalIssueName = issueName.toLowerCase();
        if (issueNameLen == targetNameLen) {
            return targetName.equals(canonicalIssueName);
        } else if (targetNameLen > issueNameLen) {
            return targetName.endsWith("." + canonicalIssueName);
        } else {
            return canonicalIssueName.endsWith("." + targetName);
        }
    }

    private static <T> T firstOrElse(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }

    @Data
    private static class QueryUsages {
        final Set<String> methods = new TreeSet<>();
        final List<List<ParameterSetOperation>> operations = new ArrayList<>();
    }
}
