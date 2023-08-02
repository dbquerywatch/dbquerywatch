package org.dbquerywatch.configuration.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({
        AnalyzerProperties.class,
        DatasourceProxyBeanPostProcessor.class,
})
class SpringConfiguration {
}
