package com.parolisoft.dbquerywatch;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class ExecutionPlanReporter {

    private static final Pattern NON_ANALYZABLE_STATEMENT = Pattern.compile("^call\\b", Pattern.CASE_INSENSITIVE);

    static final String GOOD_QUERY_MESSAGE = "No issues found for query {{}}";
    static final String BAD_QUERY_MESSAGE = "Issues found for query {{}}: {}";

    static final JsonMapper jsonMapper = new JsonMapper();

    private final AnalyzerSettings settings;
    private final ExecutionPlanAnalyzer analyzer;

    public static ExecutionPlanReporter create(Environment environment, DataSource dataSource) {
        AnalyzerSettings settings = importSettings(environment);
        ExecutionPlanAnalyzer analyzer = ExecutionPlanAnalyzerFactory.create(dataSource);
        return new ExecutionPlanReporter(settings, analyzer);
    }

    @SneakyThrows
    public void report(String querySql, Supplier<Object[]> parametersSupplier) {
        if (!log.isDebugEnabled() || isNonAnalyzableStatement(querySql)) {
            return;
        }
        String statementId = getStatementID();
        List<Issue> issues = analyzer.analyze(statementId, querySql, parametersSupplier.get())
            .stream().filter(issue ->
                settings.smallTables.stream().noneMatch(st -> tableNameMatch(st, issue.getObjectName()))
            )
            .collect(Collectors.toList());
        if (issues.isEmpty()) {
            log.debug(GOOD_QUERY_MESSAGE, querySql);
        } else {
            log.debug(BAD_QUERY_MESSAGE, querySql, jsonMapper.writeValueAsString(issues));
        }
    }

    private static boolean isNonAnalyzableStatement(String querySql) {
        return NON_ANALYZABLE_STATEMENT.matcher(querySql).find();
    }

    private static boolean tableNameMatch(String targetName, String issueName) {
        int issueNameLen = issueName.length();
        int targetNameLen = targetName.length();
        String canonicalIssueName = issueName.toLowerCase();
        if (issueNameLen == targetNameLen) {
            return targetName.equals(canonicalIssueName);
        } else if (targetNameLen > issueNameLen) {
            return targetName.substring(targetNameLen - issueNameLen).equals(canonicalIssueName) &&
                targetName.charAt(targetNameLen - issueNameLen - 1) == '.';
        } else {
            return canonicalIssueName.substring(issueNameLen - targetNameLen).equals(targetName) &&
                canonicalIssueName.charAt(issueNameLen - targetNameLen - 1) == '.';
        }
    }

    private static AnalyzerSettings importSettings(Environment environment) {
        AnalyzerSettings.AnalyzerSettingsBuilder builder = AnalyzerSettings.builder();
        List<String> smallTables = getPropertyList(environment, "dbquerywatch.small-tables");
        builder.smallTables(smallTables);
        return builder.build();
    }

    private static List<String> getPropertyList(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.toLowerCase().split(","));
    }

    private static String getStatementID() {
        String uuid = UUID.randomUUID().toString();
        // Oracle STATEMENT_ID capacity is 30 chars.
        return uuid.substring(uuid.length() - (24 + 3));
    }
}
