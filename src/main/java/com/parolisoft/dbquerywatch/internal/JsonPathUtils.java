package com.parolisoft.dbquerywatch.internal;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.experimental.UtilityClass;

@UtilityClass
class JsonPathUtils {

    static final Configuration JSON_PATH_CONFIGURATION = new Configuration.ConfigurationBuilder()
            .jsonProvider(new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();
}
