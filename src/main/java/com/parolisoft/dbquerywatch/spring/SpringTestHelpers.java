package com.parolisoft.dbquerywatch.spring;

import com.parolisoft.dbquerywatch.TestHelpers;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class SpringTestHelpers {

    public HttpHeaders buildTraceHeaders(Class<?> clazz) {
        HttpHeaders headers = new HttpHeaders();
        TestHelpers.buildTraceHeaders(clazz, null).forEach(headers::add);
        return headers;
    }
}
