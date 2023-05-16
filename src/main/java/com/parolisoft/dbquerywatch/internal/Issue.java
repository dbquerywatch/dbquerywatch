package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

@Value
public class Issue {
    IssueType type;
    String objectName;
    String predicate;
}
