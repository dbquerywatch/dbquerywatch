package org.dbquerywatch.application.domain.model;

import org.immutables.value.Value;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@Value.Immutable
@Value.Style(allParameters = true)
public interface Issue extends Serializable {
    IssueType getType();

    String getObjectName();

    @Nullable
    String getPredicate();
}
