package com.parolisoft.dbquerywatch.internal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Issue {
    IssueType type;
    String objectName;
    String filter;
}
