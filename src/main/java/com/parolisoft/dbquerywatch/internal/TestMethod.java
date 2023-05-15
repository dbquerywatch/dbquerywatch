package com.parolisoft.dbquerywatch.internal;

import lombok.Value;

@Value(staticConstructor = "of")
class TestMethod {
    String className;
    String methodName;
}
