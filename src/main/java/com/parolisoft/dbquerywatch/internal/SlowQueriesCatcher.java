package com.parolisoft.dbquerywatch.internal;

import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;

import java.util.List;
import java.util.stream.Collectors;

import static com.parolisoft.dbquerywatch.internal.ExecutionPlanReporter.BAD_QUERY_MESSAGE;
import static com.parolisoft.dbquerywatch.internal.ExecutionPlanReporter.GOOD_QUERY_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SlowQueriesCatcher {

    private LogCaptor logCaptor;

    public void beforeEach() {
        logCaptor = LogCaptor.forClass(ExecutionPlanReporter.class);
        logCaptor.setLogLevelToDebug();
    }

    public void afterEach() {
        logCaptor.resetLogLevel();
        List<LogEvent> events = logCaptor.getLogEvents();
        logCaptor.close();
        assertAll(
                "Potential slow queries were found",
                events.stream()
                        .filter(ev -> BAD_QUERY_MESSAGE.equals(ev.getMessage()))
                        .map(ev -> toStringList(ev.getArguments()))
                        .map(args ->
                                () -> fail(String.format("\nQuery: %s\nIssue(s): %s\n", args.get(0), Issues.fromString(args.get(1))))
                        )
        );
        boolean anyGoodMessage = events.stream()
                .anyMatch(ev -> GOOD_QUERY_MESSAGE.equals(ev.getMessage()));
        assertTrue(anyGoodMessage, "No query was capture and analyzed");
    }

    private static List<String> toStringList(List<?> objects) {
        return objects.stream().map(String::valueOf).collect(Collectors.toList());
    }
}
