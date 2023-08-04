package org.dbquerywatch.testapp.application;

import com.google.common.truth.Correspondence;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.dbquerywatch.application.domain.model.Issue;
import org.dbquerywatch.application.domain.model.SlowQueryReport;
import org.dbquerywatch.application.domain.service.ExecutionPlanAnalyzerException;
import org.dbquerywatch.application.domain.service.NoQueriesWereAnalyzed;
import org.dbquerywatch.application.domain.service.SlowQueriesFoundException;
import org.dbquerywatch.common.SqlUtils;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.testkit.engine.*;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@SuppressWarnings("SameParameterValue")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatchSlowQueriesTest {


    @RequiredArgsConstructor
    private enum DatabaseKind {
        H2(null, null),
        MySQL(MySQLDatabaseContainerInitializer.class, null),
        Oracle(OracleDatabaseContainerInitializer.class, DisabledOnMacM1.class),
        Postgres(PostgresDatabaseContainerInitializer.class, null);

        private final @Nullable Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer;
        @Nullable
        private final Class<? extends Annotation> extraAnnotation;
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    private enum ClientKind {
        Sequential(MockMvcIntegrationTests.class),
        Concurrent(WebClientIntegrationTests.class);

        private final Class<? extends BaseIntegrationTests> baseClass;
    }

    @BeforeAll
    void asyncStartAllContainers() {
        Arrays.stream(DatabaseKind.values())
            .map(db -> db.initializer)
            .filter(Objects::nonNull)
            .flatMap(initClass -> Arrays.stream(initClass.getDeclaredFields()))
            .filter(field -> JdbcDatabaseContainerInitializer.class.isAssignableFrom(field.getType())
                && ReflectionUtils.isStatic(field))
            // Force class initialization. @see https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.4.1
            .forEach(ReflectionUtils::tryToReadFieldValue);
    }

    @CartesianTest
    void should_succeed_if_no_slow_query_was_found(
        @CartesianTest.Enum ClientKind clientKind
    ) {
        Class<?> testClass = createTestClass(clientKind, DatabaseKind.H2);
        runIntegrationTests(testClass, 1, null, TagFilter.excludeTags("slow-query"));
    }

    @CartesianTest
    void should_catch_slow_queries_for_all_supported_databases_including_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Class<?> testClass = createTestClass(clientKind, databaseKind);

        // When
        SlowQueriesFoundException ex = runIntegrationTests(testClass, 3, SlowQueriesFoundException.class);
        List<SlowQueryReport> reports = ex.getSlowQueries();

        // Then
        assertThat(reports).hasSize(2);

        assertThat(reports.stream()
            .flatMap(rep -> rep.getMethods().stream()))
            .containsExactly(
                "org.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query",
                "org.dbquerywatch.testapp.adapters.db.DefaultJournalRepository::findByPublisher"
            );

        List<String> tableNames = reports.stream()
            .flatMap(rep -> rep.getIssues().stream()
                .map(Issue::getObjectName))
            .toList();

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles", "journals");
    }

    @CartesianTest
    void should_catch_slow_queries_for_all_supported_databases_excluding_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Class<?> testClass = createTestClass(clientKind, databaseKind, "dbquerywatch.small-tables=journals");

        // When
        SlowQueriesFoundException ex = runIntegrationTests(testClass, 3, SlowQueriesFoundException.class);
        List<SlowQueryReport> reports = ex.getSlowQueries();

        // Then
        assertThat(reports).hasSize(1);

        SlowQueryReport singleReport = reports.get(0);

        assertThat(singleReport.getMethods())
            .containsExactly("org.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query");

        List<String> tableNames = singleReport.getIssues().stream()
            .map(Issue::getObjectName)
            .toList();

        assertThat(tableNames)
            .hasSize(1);

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles");
    }

    @Test
    void should_throw_exception_if_no_queries_were_analyzed() {
        runIntegrationTests(NoQueriesWereAnalyzedTests.class, 1, NoQueriesWereAnalyzed.class);
    }

    @Test
    void should_throw_exception_if_postgres_is_misconfigured() {
        Class<?> testClass = createTestClass(DatabaseKind.Postgres.toString(),
            MockMvcIntegrationTests.class, PostgresDatabaseMisconfiguredContainerInitializer.class, null);
        ExecutionPlanAnalyzerException ex = runIntegrationTests(testClass, 3, ExecutionPlanAnalyzerException.class);
        assertThat(ex.getLocalizedMessage()).contains("ENABLE_SEQSCAN");
    }

    private Class<?> createTestClass(ClientKind clientKind, DatabaseKind databaseKind, String... properties) {
        return createTestClass(databaseKind.toString(), clientKind.baseClass, databaseKind.initializer,
                databaseKind.extraAnnotation, properties);
    }

    @SuppressWarnings("resource")
    private Class<?> createTestClass(
        String databaseName,
        Class<? extends BaseIntegrationTests> baseClass,
        Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer,
        @Nullable Class<? extends Annotation> extraAnnotation,
        String... properties
    ) {
        List<AnnotationDescription> annotations = new ArrayList<>();
        annotations.add(
            AnnotationDescription.Builder.ofType(ActiveProfiles.class)
                .defineArray("value", databaseName.toLowerCase(Locale.US))
                .build()
        );
        if (initializer != null) {
            annotations.add(
                AnnotationDescription.Builder.ofType(ContextConfiguration.class)
                    .defineTypeArray("initializers", initializer)
                    .build()
            );
        }
        if (properties.length > 0) {
            annotations.add(
                AnnotationDescription.Builder.ofType(TestPropertySource.class)
                    .defineArray("properties", properties)
                    .build()
            );
        }
        if (extraAnnotation != null) {
            annotations.add(AnnotationDescription.Builder.ofType(extraAnnotation).build());
        }
        return new ByteBuddy()
            .subclass(baseClass)
            .annotateType(annotations)
            .make()
            .load(getClass().getClassLoader())
            .getLoaded();
    }

    @Contract("_, _, null, _ -> null; _, _, !null, _ -> !null")
    private static <T extends RuntimeException> T runIntegrationTests(
        Class<?> testClass,
        int numTests,
        @Nullable Class<T> expectedExceptionClass,
        Filter<?>... filters
    ) {
        EngineExecutionResults engineResults = EngineTestKit
            .engine("junit-jupiter")
            // reactivate @Deactivated classes
            .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
            .selectors(selectClass(testClass))
            .filters(filters)
            .execute();

        Events containerEvents = engineResults.containerEvents();
        assumeTrue(containerEvents.skipped().count() < containerEvents.started().stream().count());

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
