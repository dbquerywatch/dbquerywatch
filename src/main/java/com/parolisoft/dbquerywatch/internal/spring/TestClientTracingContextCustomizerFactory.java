package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.junit5.CatchSlowQueries;
import com.parolisoft.dbquerywatch.spring.SpringTestHelpers;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
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

import javax.annotation.Nullable;
import java.util.List;

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

    @RequiredArgsConstructor
    // CRITICAL, otherwise the spring context won't be cached!
    // @see https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html
    @EqualsAndHashCode
    private static class WebTestClientTracingContextCustomizer implements ContextCustomizer {
        private final Class<?> testClass;

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
    }

    @RequiredArgsConstructor
    private static class WebTestClientTracingCustomizer implements WebTestClientBuilderCustomizer {

        private final Class<?> testClass;

        @Override
        public void customize(WebTestClient.Builder builder) {
            builder.defaultHeaders(headers -> SpringTestHelpers.addTraceHeaders(headers, testClass));
        }
    }
}
