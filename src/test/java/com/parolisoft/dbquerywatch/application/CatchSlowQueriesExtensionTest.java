package com.parolisoft.dbquerywatch.application;

import com.google.common.truth.Correspondence;
import com.parolisoft.dbquerywatch.internal.Issue;
import com.parolisoft.dbquerywatch.internal.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.SlowQueryReport;
import com.parolisoft.dbquerywatch.internal.SqlUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@SuppressWarnings("SameParameterValue")
public class CatchSlowQueriesExtensionTest {

    @ParameterizedTest
    @MethodSource("testClassProvider")
    public void should_catch_slow_queries_for_all_supported_databases_including_small_tables(Class<? extends IntegrationTests> testClass) {
        Map<String, String> properties = Map.of(
            "dbquerywatch.small-tables", ""
        );
        List<SlowQueryReport> reports = runIntegrationTests(testClass, properties, 3);

        assertThat(reports).hasSize(2);

        assertThat(reports.stream().flatMap(rep -> rep.getMethods().stream()))
            .containsExactly(
                "com.parolisoft.dbquerywatch.adapters.db.DefaultArticleRepository::query",
                "com.parolisoft.dbquerywatch.adapters.db.DefaultJournalRepository::findByPublisher"
            );

        List<String> tableNames = reports.stream().flatMap(rep -> rep.getIssues().stream().map(Issue::getObjectName)).toList();

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles", "journals");
    }

    @ParameterizedTest
    @MethodSource("testClassProvider")
    public void should_catch_slow_queries_for_all_supported_databases_excluding_small_tables(Class<? extends IntegrationTests> testClass) {
        Map<String, String> properties = Map.of(
            "dbquerywatch.small-tables", "journals"
        );
        List<SlowQueryReport> reports = runIntegrationTests(testClass, properties, 3);

        assertThat(reports).hasSize(1);

        SlowQueryReport singleReport = reports.get(0);

        assertThat(singleReport.getMethods())
            .containsExactly("com.parolisoft.dbquerywatch.adapters.db.DefaultArticleRepository::query");

        List<String> tableNames = singleReport.getIssues().stream().map(Issue::getObjectName).toList();

        assertThat(tableNames)
            .hasSize(1);

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles");
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

    private static List<SlowQueryReport> runIntegrationTests(
        Class<? extends IntegrationTests> testClass,
        Map<String, String> properties,
        int numTests
    ) {
        EngineExecutionResults engineResults = EngineTestKit
            .engine("junit-jupiter")
            // reactivate @Deactivated classes
            .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
            .configurationParameters(properties)
            .selectors(selectClass(testClass))
            .execute();

        engineResults
            .testEvents()
            .assertStatistics(stats -> stats.started(numTests).failed(0).succeeded(numTests));

        List<Event> events = engineResults.containerEvents().stream()
            .filter(ev -> ev.getTestDescriptor() instanceof ClassTestDescriptor && ev.getType() == EventType.FINISHED)
            .toList();

        assertThat(events)
            .hasSize(1);

        Event classFinishedEvent = events.get(0);
        TestExecutionResult executionResult = classFinishedEvent.getRequiredPayload(TestExecutionResult.class);
        assertThat(executionResult.getStatus()).isEqualTo(FAILED);
        Optional<Throwable> throwable = executionResult.getThrowable();
        assertThat(throwable).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(throwable.get()).isInstanceOf(SlowQueriesFoundException.class);

        return ((SlowQueriesFoundException) throwable.get()).getSlowQueries();
    }
}
