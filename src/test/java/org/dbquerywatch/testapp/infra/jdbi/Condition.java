package org.dbquerywatch.testapp.infra.jdbi;

import org.jdbi.v3.core.statement.Query;

import java.util.function.Consumer;

public record Condition(String predicate, Consumer<Query> customizer) {
}
