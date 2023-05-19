package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Value
public class Issue {
    @Nonnull IssueType type;
    @Nonnull String objectName;
    @Nullable String predicate;
}
