package org.dbquerywatch.configuration.spring;

import org.dbquerywatch.api.helpers.SpringTestHelpers;
import org.dbquerywatch.api.spring.junit5.CatchSlowQueries;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.web.reactive.server.WebTestClientBuilderCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;

import static org.springframework.util.StringUtils.uncapitalize;

class TestClientTracingContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    @Nullable
    public ContextCustomizer createContextCustomizer(
        Class<?> testClass,
        List<ContextConfigurationAttributes> configAttributes
    ) {
        CatchSlowQueries annotation = TestContextAnnotationUtils.findMergedAnnotation(testClass, CatchSlowQueries.class);
        if (annotation == null) {
            return null;
        }
        return new WebTestClientTracingContextCustomizer(testClass);
    }

    private static class WebTestClientTracingContextCustomizer implements ContextCustomizer {
        private final Class<?> testClass;

        private WebTestClientTracingContextCustomizer(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public void customizeContext(
            ConfigurableApplicationContext context,
            MergedContextConfiguration mergedConfig
        ) {
            ((BeanDefinitionRegistry) context.getBeanFactory())
                .registerBeanDefinition(uncapitalize(WebTestClientTracingCustomizer.class.getSimpleName()),
                    BeanDefinitionBuilder.genericBeanDefinition(
                        WebTestClientTracingCustomizer.class,
                        () -> new WebTestClientTracingCustomizer(testClass)
                    ).getBeanDefinition()
                );
        }

        // Equals & HashCode are CRITICAL, otherwise the spring context won't be cached!
        // @see https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WebTestClientTracingContextCustomizer that = (WebTestClientTracingContextCustomizer) o;
            return Objects.equals(testClass, that.testClass);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(testClass);
        }
    }

    private static class WebTestClientTracingCustomizer implements WebTestClientBuilderCustomizer {

        private final Class<?> testClass;

        private WebTestClientTracingCustomizer(Class<?> testClass) {
            this.testClass = testClass;
        }

        @Override
        public void customize(WebTestClient.Builder builder) {
            builder.defaultHeaders(headers -> SpringTestHelpers.addTraceHeaders(headers, testClass));
        }
    }
}
