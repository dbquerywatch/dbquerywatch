package com.parolisoft.dbquerywatch;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
class Issue {
    IssueType type;
    String objectName;
    String filter;
}
