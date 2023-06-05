package com.parolisoft.dbquerywatch.application;

import com.google.common.truth.Correspondence;
import com.parolisoft.dbquerywatch.SlowQueriesFoundException;
import com.parolisoft.dbquerywatch.internal.Issue;
import com.parolisoft.dbquerywatch.internal.SlowQueryReport;
import com.parolisoft.dbquerywatch.internal.SqlUtils;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
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
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
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
        SameThread(MockMvcIntegrationTests.class),
        AnotherThread(WebClientIntegrationTests.class);

        private final Class<? extends BaseIntegrationTests> baseClass;
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
        Class<?> testClass = createTestClass(databaseKind, clientKind);

        // When
        List<SlowQueryReport> reports = runIntegrationTests(testClass, properties, 3);

        // Then
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

    @CartesianTest
    public void should_catch_slow_queries_for_all_supported_databases_excluding_small_tables(
        @CartesianTest.Enum DatabaseKind databaseKind,
        @CartesianTest.Enum ClientKind clientKind
    ) {
        // Given
        Map<String, String> properties = Map.of(
            "dbquerywatch.small-tables", "journals"
        );
        Class<?> testClass = createTestClass(databaseKind, clientKind);

        // When
        List<SlowQueryReport> reports = runIntegrationTests(testClass, properties, 3);

        // Then
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

    @SuppressWarnings("resource")
    private Class<?> createTestClass(DatabaseKind databaseKind, ClientKind clientKind) {
        if (databaseKind.initializer == null) {
            return clientKind.baseClass;
        }
        return new ByteBuddy()
            .subclass(clientKind.baseClass)
            .annotateType(
                AnnotationDescription.Builder.ofType(ActiveProfiles.class)
                    .defineArray("value", databaseKind.toString().toLowerCase(Locale.US))
                    .build()
            )
            .annotateType(
                AnnotationDescription.Builder.ofType(ContextConfiguration.class)
                    .defineTypeArray("initializers", databaseKind.initializer)
                    .build()
            )
            .make()
            .load(getClass().getClassLoader())
            .getLoaded();
    }

    private static List<SlowQueryReport> runIntegrationTests(
        Class<?> testClass,
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
