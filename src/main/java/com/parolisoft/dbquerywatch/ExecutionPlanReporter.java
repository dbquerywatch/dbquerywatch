package com.parolisoft.dbquerywatch;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
class ExecutionPlanReporter {

    static final String GOOD_QUERY_MESSAGE = "No issues found for query {{}}";
    static final String BAD_QUERY_MESSAGE = "Issues found for query {{}}: {}";

    static final JsonMapper jsonMapper = new JsonMapper();

    private final ExecutionPlanAnalyzer analyzer;

    public static ExecutionPlanReporter create(DataSource dataSource) {
        ExecutionPlanAnalyzer analyzer = ExecutionPlanAnalyzerFactory.create(dataSource);
        return new ExecutionPlanReporter(analyzer);
    }

    @SneakyThrows
    public void report(String querySql, Supplier<Object[]> parametersSupplier) {
        if (!log.isDebugEnabled()) {
            return;
        }
        String statementId = getStatementID();
        List<Issue> issues = analyzer.analyze(statementId, querySql, parametersSupplier.get());
        if (issues.isEmpty()) {
            log.debug(GOOD_QUERY_MESSAGE, querySql);
        } else {
            log.debug(BAD_QUERY_MESSAGE, querySql, jsonMapper.writeValueAsString(issues));
        }
    }

    private static String getStatementID() {
        String uuid = UUID.randomUUID().toString();
        // Oracle STATEMENT_ID capacity is 30 chars.
        return uuid.substring(uuid.length() - (24 - 3));
    }
}
