package org.dbquerywatch.testapp.application;

import com.google.common.truth.Correspondence;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import one.util.streamex.StreamEx;
import org.dbquerywatch.application.domain.model.PerStatementIssuesReport;
import org.dbquerywatch.application.domain.model.ReportElement;
import org.dbquerywatch.application.domain.model.SeqScan;
import org.dbquerywatch.application.domain.service.DatabasePerformanceIssuesDetectedException;
import org.dbquerywatch.common.SqlUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventType;
import org.junit.platform.testkit.engine.Events;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@SuppressWarnings("SameParameterValue")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbQueryWatchExtensionTest {

    private enum DatabaseKind {
        H2(null, null),
        MySQL(MySQLDatabaseContainerInitializer.class, null),
        Oracle(OracleDatabaseContainerInitializer.class, DisabledOnMacArm.class),
        Postgres(PostgresDatabaseContainerInitializer.class, null);

        private final @Nullable Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer;
        @Nullable
        private final Class<? extends Annotation> extraAnnotation;

        DatabaseKind(
            @Nullable Class<? extends ApplicationContextInitializer<ConfigurableApplicationContext>> initializer,
            @Nullable Class<? extends Annotation> extraAnnotation
        ) {
            this.initializer = initializer;
            this.extraAnnotation = extraAnnotation;
        }
    }

    @SuppressWarnings("unused")
    private enum ClientKind {
        // When using WebEnvironment.MOCK, client and server run on the same thread
        // Strategy to correlated Query to TestMethod uses ThreadLocal
        SameThread(MockMvcIntegrationTests.class),
        // When using WebEnvironment.RANDOM_PORT and a client *other* than MockMvc, client and server run on different threads
        // Strategy to correlated Query to TestMethod uses MDC managed by a tracing library
        OtherThread(WebClientIntegrationTests.class);

        private final Class<? extends BaseIntegrationTests> baseClass;

        ClientKind(Class<? extends BaseIntegrationTests> baseClass) {
            this.baseClass = baseClass;
        }
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
        runIntegrationTests(testClass, 1, 0, null, TagFilter.excludeTags("slow-query"));
    }

    @CartesianTest
    void should_catch_slow_queries_for_all_supported_databases_including_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Class<?> testClass = createTestClass(clientKind, databaseKind);

        // When
        List<ReportElement> reports = StreamEx.of(runIntegrationTests(testClass, 3, 2, DatabasePerformanceIssuesDetectedException.class))
            .flatCollection(DatabasePerformanceIssuesDetectedException::getReports)
            .toList();

        List<PerStatementIssuesReport> perStatementReports = StreamEx.of(reports)
            .select(PerStatementIssuesReport.class)
            .toList();

        // Then
        assertThat(perStatementReports).hasSize(2);

        assertThat(perStatementReports.stream()
            .flatMap(rep -> rep.getMethods().stream()))
            .containsExactly(
                "org.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query",
                "org.dbquerywatch.testapp.adapters.db.DefaultJournalRepository::findByPublisher"
            );

        List<String> tableNames = perStatementReports.stream()
            .flatMap(rep -> rep.getSeqScans().stream()
                .map(SeqScan::getObjectName))
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
        List<ReportElement> reports = StreamEx.of(runIntegrationTests(testClass, 3, 1, DatabasePerformanceIssuesDetectedException.class))
            .flatCollection(DatabasePerformanceIssuesDetectedException::getReports)
            .toList();

        List<PerStatementIssuesReport> perStatementReports = StreamEx.of(reports)
            .select(PerStatementIssuesReport.class)
            .toList();

        // Then
        assertThat(perStatementReports).hasSize(1);

        PerStatementIssuesReport singleReport = perStatementReports.get(0);

        assertThat(singleReport.getMethods())
            .containsExactly("org.dbquerywatch.testapp.adapters.db.DefaultArticleRepository::query");

        List<String> tableNames = singleReport.getSeqScans().stream()
            .map(SeqScan::getObjectName)
            .toList();

        assertThat(tableNames)
            .hasSize(1);

        assertThat(tableNames)
            .comparingElementsUsing(Correspondence.from(SqlUtils::tableNameMatch, "equivalent table name"))
            .containsExactly("articles");
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

    private static <T extends RuntimeException> List<T> runIntegrationTests(
        Class<?> testClass,
        int numTests,
        int expectedFailures,
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

        Events testEvents = engineResults.testEvents();

        testEvents.assertStatistics(stats -> stats
            .started(numTests)
            .failed(expectedFailures)
            .succeeded(numTests - expectedFailures)
        );

        List<Throwable> throwables = testEvents.stream()
            .filter(ev -> ev.getTestDescriptor() instanceof TestMethodTestDescriptor && ev.getType() == EventType.FINISHED)
            .map(ev -> ev.getRequiredPayload(TestExecutionResult.class))
            .filter(result -> result.getStatus() == FAILED)
            .<Throwable>mapMulti((result, c) -> result.getThrowable().ifPresent(c))
            .toList();

        assertThat(throwables)
            .hasSize(expectedFailures);

        return throwables.stream()
            .map(throwable -> {
                //noinspection DataFlowIssue
                assertThat(throwable).isInstanceOf(expectedExceptionClass);
                return expectedExceptionClass.cast(throwable);
            })
            .toList();
    }
}
