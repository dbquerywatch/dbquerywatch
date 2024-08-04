package org.dbquerywatch.testapp.domain;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Value.Immutable
public interface Article {
    Long getId();

    LocalDate getPublishedAt();

    String getAuthorFullName();

    String getAuthorLastName();

    String getTitle();

    @Nullable
    Journal getJournal();
}
