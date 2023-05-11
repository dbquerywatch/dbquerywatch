package com.parolisoft.dbquerywatch;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.ExecutionPlanReporter.BAD_QUERY_MESSAGE;
import static com.parolisoft.dbquerywatch.ExecutionPlanReporter.GOOD_QUERY_MESSAGE;
import static com.parolisoft.dbquerywatch.ExecutionPlanReporter.jsonMapper;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class CatchSlowQueriesExtension implements BeforeEachCallback, AfterEachCallback {

    private LogCaptor logCaptor;

    @Override
    public void beforeEach(ExtensionContext context) {
        logCaptor = LogCaptor.forClass(ExecutionPlanReporter.class);
        logCaptor.setLogLevelToDebug();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        logCaptor.resetLogLevel();
        List<LogEvent> events = logCaptor.getLogEvents();
        logCaptor.close();
        assertAll(
                "Potential slow queries were found",
                events.stream()
                        .filter(ev -> BAD_QUERY_MESSAGE.equals(ev.getMessage()))
                        .map(ev -> toStringList(ev.getArguments()))
                        .map(args ->
                                () -> fail(String.format("\nQuery: %s\nIssue(s): %s\n", args.get(0), parseIssueList(args.get(1))))
                        )
        );
        boolean anyGoodMessage = events.stream()
                .anyMatch(ev -> GOOD_QUERY_MESSAGE.equals(ev.getMessage()));
        assertTrue(anyGoodMessage, "No query was capture and analyzed");
    }

    private static List<String> toStringList(List<?> objects) {
        return objects.stream().map(String::valueOf).collect(Collectors.toList());
    }

    @SneakyThrows
    private List<Issue> parseIssueList(String json) {
        return jsonMapper.readerForListOf(Issue.class).readValue(json);
    }
}
