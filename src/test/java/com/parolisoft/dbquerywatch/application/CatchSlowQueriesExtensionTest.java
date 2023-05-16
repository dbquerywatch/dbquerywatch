package com.parolisoft.dbquerywatch.application;

import com.parolisoft.dbquerywatch.internal.SlowQueriesFoundException;
import org.assertj.core.api.Condition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventType;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.parolisoft.dbquerywatch.internal.SqlUtils.tableNameMatch;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.isEqual;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.type;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

public class CatchSlowQueriesExtensionTest {

    @ParameterizedTest
    @MethodSource("testClassProvider")
    @SuppressWarnings("unchecked")
    public void should_catch_slow_queries_for_all_supported_databases(Class<? extends IntegrationTests> testClass) {
        EngineExecutionResults results = EngineTestKit
            .engine("junit-jupiter")
            .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
            .selectors(selectClass(testClass))
            .execute();

        results
            .testEvents()
            .assertStatistics(stats -> stats.started(2).failed(0).succeeded(2));

        Condition<? extends Throwable> throwableCondition = new Condition<>(
            where((SlowQueriesFoundException e) -> e.getSlowQueries().size(), isEqual(1))
                .and(where(e -> e.getSlowQueries().get(0).getMethods(), isEqual(singleton("should_find_article_by_year_range"))))
                .and(where(e -> e.getSlowQueries().get(0).getIssues().size(), isEqual(1)))
                .and(where(e -> e.getSlowQueries().get(0).getIssues().get(0).getObjectName(), doesTableNameMatch("articles"))),
            "message is '%s'"
        );
        results
            .allEvents()
            .assertThatEvents()
            .filteredOn(container(testClass))
            .filteredOn(type(EventType.FINISHED))
            .have(event(
                container(),
                finishedWithFailure(
                    instanceOf(SlowQueriesFoundException.class),
                    (Condition<Throwable>) throwableCondition,
                    new Condition<>(e -> ((SlowQueriesFoundException) e).getSlowQueries().size() == 1, "")
                )
            ));
    }

    private static Stream<Class<? extends IntegrationTests>> testClassProvider() {
        return Stream.of(
            H2IntegrationTests.class,
            MySQLIntegrationTests.class,
            OracleIntegrationTests.class,
            PostgresIntegrationTests.class
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static Predicate<String> doesTableNameMatch(@Nonnull String targetRef) {
        return object -> tableNameMatch(object, targetRef);
    }
}
