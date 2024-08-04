package org.dbquerywatch.testapp.application.service;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

@Value.Immutable
public interface ArticleQuery {
    @Nullable
    String getAuthorLastName();

    @Nullable
    Integer getFromYear();

    @Nullable
    Integer getToYear();

    @Nullable
    String getJournalName();
}
