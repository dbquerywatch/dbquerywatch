package org.dbquerywatch.testapp.infra.jdbi;

import lombok.Value;
import lombok.experimental.Accessors;
import org.jdbi.v3.core.statement.Query;

import java.util.function.Consumer;

@Value
@Accessors(fluent = true)
@SuppressWarnings("ClassCanBeRecord")   // sonarqube parser cannot parse record properly
public class Condition {
    String predicate;
    Consumer<Query> customizer;
}
