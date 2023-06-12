package com.parolisoft.dbquerywatch.testapp.application;

import com.google.common.truth.Correspondence;
import com.parolisoft.dbquerywatch.ExecutionPlanAnalyzerException;
import com.parolisoft.dbquerywatch.NoQueriesWereAnalyzed;
import com.parolisoft.dbquerywatch.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.Issue;
import com.parolisoft.dbquerywatch.internal.SlowQueryReport;
import com.parolisoft.dbquerywatch.internal.SqlUtils;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.EventType;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.util.Collections.emptyMap;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@SuppressWarnings("SameParameterValue")
public class CatchSlowQueriesTest {

    @RequiredArgsConstructor
    private enum DatabaseKind {
        H2(null),
        MySQL(MySQLDatabaseContainerInitializer.class),
        Oracle(OracleDatabaseContainerInitializer.class),
        Postgres(PostgresDatabaseContainerInitializer.class);

        private final @Nullable Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer;
    }

    @RequiredArgsConstructor
    private enum ClientKind {
        Sequential(MockMvcIntegrationTests.class),
        Concurrent(WebClientIntegrationTests.class);

        private final Class<? extends BaseIntegrationTests> baseClass;
    }

    @CartesianTest
    public void should_succeed_if_no_slow_query_was_found(
        @CartesianTest.Enum ClientKind clientKind
    ) {
        Class<?> testClass = createTestClass(clientKind, DatabaseKind.H2);
        runIntegrationTests(testClass, emptyMap(), 1, null, TagFilter.excludeTags("slow-query"));
    }

    @CartesianTest
    public void should_catch_slow_queries_for_all_supported_databases_including_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Map<String, String> properties = Map.of(
            "dbquerywatch.small-tables", ""
        );
        Class<?> testClass = createTestClass(clientKind, databaseKind);

        // When
        SlowQueriesFoundException ex = runIntegrationTests(testClass, properties, 3, SlowQueriesFoundException.class);
        List<SlowQueryReport> reports = ex.getSlowQueries();

        // Then
        assertThat(reports).hasSize(2);

        assertThat(reports.stream().flatMap(rep -> rep.getMethods().stream()))
            .containsExactly(
                "com.parolisoft.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query",
                "com.parolisoft.dbquerywatch.testapp.adapters.db.DefaultJournalRepository::findByPublisher"
            );

        List<String> tableNames = reports.stream().flatMap(rep -> rep.getIssues().stream().map(Issue::getObjectName)).toList();

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles", "journals");
    }

    @CartesianTest
    public void should_catch_slow_queries_for_all_supported_databases_excluding_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Map<String, String> properties = Map.of(
            "dbquerywatch.small-tables", "journals"
        );
        Class<?> testClass = createTestClass(clientKind, databaseKind);

        // When
        SlowQueriesFoundException ex = runIntegrationTests(testClass, properties, 3, SlowQueriesFoundException.class);
        List<SlowQueryReport> reports = ex.getSlowQueries();

        // Then
        assertThat(reports).hasSize(1);

        SlowQueryReport singleReport = reports.get(0);

        assertThat(singleReport.getMethods())
            .containsExactly("com.parolisoft.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query");

        List<String> tableNames = singleReport.getIssues().stream().map(Issue::getObjectName).toList();

        assertThat(tableNames)
            .hasSize(1);

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles");
    }

    @Test
    public void should_throw_exception_if_no_queries_were_analyzed() {
        runIntegrationTests(NoQueriesWereAnalyzedTests.class, emptyMap(), 1, NoQueriesWereAnalyzed.class);
    }

    @Test
    public void should_throw_exception_if_postgres_is_misconfigured() {
        Class<?> testClass = createTestClass(DatabaseKind.Postgres.toString(),
            MockMvcIntegrationTests.class, PostgresDatabaseMisconfiguredContainerInitializer.class);
        ExecutionPlanAnalyzerException ex = runIntegrationTests(testClass, emptyMap(), 3, ExecutionPlanAnalyzerException.class);
        assertThat(ex.getLocalizedMessage()).contains("ENABLE_SEQSCAN");
    }

    private Class<?> createTestClass(ClientKind clientKind, DatabaseKind databaseKind) {
        return createTestClass(databaseKind.toString(), clientKind.baseClass, databaseKind.initializer);
    }

    @SuppressWarnings("resource")
    private Class<?> createTestClass(
        String databaseName,
        Class<? extends BaseIntegrationTests> baseClass,
        Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer
    ) {
        if (initializer == null) {
            return baseClass;
        }
        return new ByteBuddy()
            .subclass(baseClass)
            .annotateType(
                AnnotationDescription.Builder.ofType(ActiveProfiles.class)
                    .defineArray("value", databaseName.toLowerCase(Locale.US))
                    .build()
            )
            .annotateType(
                AnnotationDescription.Builder.ofType(ContextConfiguration.class)
                    .defineTypeArray("initializers", initializer)
                    .build()
            )
            .make()
            .load(getClass().getClassLoader())
            .getLoaded();
    }

    @Contract("_, _, _, null, _ -> null; _, _, _, !null, _ -> !null")
    private static <T extends RuntimeException> T runIntegrationTests(
        Class<?> testClass,
        Map<String, String> properties,
        int numTests,
        @Nullable Class<T> expectedExceptionClass,
        Filter<?>... filters
    ) {
        EngineExecutionResults engineResults = EngineTestKit
            .engine("junit-jupiter")
            // reactivate @Deactivated classes
            .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
            .configurationParameters(properties)
            .selectors(selectClass(testClass))
            .filters(filters)
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
        if (expectedExceptionClass == null) {
            assertThat(executionResult.getStatus()).isEqualTo(SUCCESSFUL);
            return null;
        }
        assertThat(executionResult.getStatus()).isEqualTo(FAILED);
        Optional<Throwable> throwable = executionResult.getThrowable();
        assertThat(throwable).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(throwable.get()).isInstanceOf(expectedExceptionClass);

        return expectedExceptionClass.cast(throwable.get());
    }
}
