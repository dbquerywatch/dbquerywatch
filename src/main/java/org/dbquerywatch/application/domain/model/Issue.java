package org.dbquerywatch.application.domain.model;

import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

@Value
public class Issue implements Serializable {
    @Nonnull IssueType type;
    @Nonnull String objectName;
    @Nullable String predicate;
}
