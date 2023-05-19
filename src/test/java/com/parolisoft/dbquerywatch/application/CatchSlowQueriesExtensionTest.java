package com.parolisoft.dbquerywatch.application;

import com.parolisoft.dbquerywatch.internal.Issue;
import com.parolisoft.dbquerywatch.internal.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.SlowQueryReport;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventType;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.parolisoft.dbquerywatch.infra.assertj.ActualReturningAssert.actualReturning;
import static com.parolisoft.dbquerywatch.internal.SqlUtils.tableNameMatch;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.type;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

public class CatchSlowQueriesExtensionTest {

    @ParameterizedTest
    @MethodSource("testClassProvider")
    public void should_catch_slow_queries_for_all_supported_databases(Class<? extends IntegrationTests> testClass) {
        EngineExecutionResults engineResults = EngineTestKit
            .engine("junit-jupiter")
            .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
            .selectors(selectClass(testClass))
            .execute();

        engineResults
            .testEvents()
            .assertStatistics(stats -> stats.started(3).failed(0).succeeded(3));

        Event classFinishedEvent = engineResults
            .allEvents()
            .assertThatEvents()
            .filteredOn(container(testClass))
            .filteredOn(type(EventType.FINISHED))
            .singleElement(as(actualReturning(Event.class)))
            .has(event(finishedWithFailure(instanceOf(SlowQueriesFoundException.class))))
            .getActual();

        ObjectAssert<SlowQueryReport> slowQueryReport =
            assertThat(classFinishedEvent.getRequiredPayload(TestExecutionResult.class).getThrowable())
                .get(as(type(SlowQueriesFoundException.class)))
                .extracting(SlowQueriesFoundException::getSlowQueries)
                .asList()
                .singleElement(as(type(SlowQueryReport.class)));

        slowQueryReport
            .extracting(SlowQueryReport::getMethods)
            .isEqualTo(singleton("should_find_article_by_year_range"));

        slowQueryReport
            .extracting(SlowQueryReport::getIssues)
            .asList()
            .singleElement(as(type(Issue.class)))
            .extracting(Issue::getObjectName)
            .is(isTableNameCompatibleCondition("articles"));
    }

    private static Stream<Named<Class<? extends IntegrationTests>>> testClassProvider() {
        return Stream.of(
                H2IntegrationTests.class,
                MySQLIntegrationTests.class,
                OracleIntegrationTests.class,
                PostgresIntegrationTests.class
            )
            .map(cls -> Named.of(cls.getSimpleName(), cls));
    }

    @SuppressWarnings("SameParameterValue")
    private static Condition<String> isTableNameCompatibleCondition(@Nonnull String targetRef) {
        return new Condition<>(object -> tableNameMatch(object, targetRef), targetRef);
    }
}
