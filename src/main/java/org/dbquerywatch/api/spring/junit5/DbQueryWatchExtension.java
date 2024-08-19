package org.dbquerywatch.api.spring.junit5;

import com.google.errorprone.annotations.Var;
import org.dbquerywatch.api.helpers.TestHelpers;
import org.dbquerywatch.application.domain.service.AnalyzerSettings;
import org.dbquerywatch.application.domain.service.ExecutionPlanManager;
import org.dbquerywatch.application.domain.service.ImmutableLimits;
import org.dbquerywatch.application.domain.service.Limits;
import org.dbquerywatch.application.domain.service.TestMethodIdRepository;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.beans.factory.support.BeanDefinitionOverrideException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

import static org.dbquerywatch.common.MethodUtils.invokeMethod1;
import static org.dbquerywatch.common.MethodUtils.invokeStaticMethod1;

@SuppressWarnings("SameParameterValue")
class DbQueryWatchExtension implements BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterEachCallback,
    ParameterResolver {

    @Override
    public void beforeAll(ExtensionContext context) {
        registerExecutionPlanManager(context);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        TestMethodIdRepository.save(context.getUniqueId());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getElement()
            .flatMap(DbQueryWatchExtension::findDbQueryWatchAnnotation)
            .ifPresent(dbQueryWatch ->
                getExecutionPlanManager(context).verifyAll(context.getUniqueId(), dbQueryWatch)
            );
        TestMethodIdRepository.clear();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException {
        return WebTestClient.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context)
        throws ParameterResolutionException {
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        WebTestClient webTestClient = springContext.getBean(WebTestClient.class);
        return webTestClient.mutate()
            .defaultHeaders(headers -> TestHelpers.buildTraceHeaders(context.getUniqueId(), null).forEach(headers::add))
            .build();
    }

    private static Optional<Limits> findDbQueryWatchAnnotation(AnnotatedElement annotatedElement) {
        MergedAnnotation<DbQueryWatch> mergedAnnotation = getMergedAnnotations(annotatedElement, DbQueryWatch.class);
        if (mergedAnnotation.isPresent()) {
            DbQueryWatch anno = mergedAnnotation.synthesize();
            return Optional.of(ImmutableLimits.builder()
                .allowSeqScans(anno.allowSeqScans())
                .maxOverallCost(anno.maxOverallCost())
                .build());
        }
        return Optional.empty();
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Annotation> MergedAnnotation<T> getMergedAnnotations(
        AnnotatedElement annotatedElement,
        Class<T> annotationType
    ) {
        @Var MergedAnnotations mergedAnnotations = MergedAnnotations.from(annotatedElement.getAnnotations());
        if (!mergedAnnotations.isPresent(annotationType) && (annotatedElement instanceof Method)) {
            Class<?> declaringClass = ((Method) annotatedElement).getDeclaringClass();
            try {
                mergedAnnotations = getMergedAnnotationsForSpring5(declaringClass);
            } catch (NoSuchFieldError ignored) {
                mergedAnnotations = getMergedAnnotationsForSpring6(declaringClass);
            }
        }
        return mergedAnnotations.get(annotationType);
    }

    private static MergedAnnotations getMergedAnnotationsForSpring5(Class<?> clazz) {
        return MergedAnnotations.from(clazz, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY_AND_ENCLOSING_CLASSES);
    }

    private static MergedAnnotations getMergedAnnotationsForSpring6(Class<?> clazz) {
        try {
            @Var Object search = invokeStaticMethod1(MergedAnnotations.class, "search",
                MergedAnnotations.SearchStrategy.class, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
            search = invokeMethod1(search, "withEnclosingClasses", Predicate.class,
                (Predicate<Class<?>>) ClassUtils::isInnerClass);
            return (MergedAnnotations) invokeMethod1(search, "from", AnnotatedElement.class, clazz);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void registerExecutionPlanManager(ExtensionContext context) {
        GenericApplicationContext springContext = (GenericApplicationContext) SpringExtension.getApplicationContext(context);
        String beanName = ExecutionPlanManager.class.getName();
        if (!springContext.containsBean(beanName)) {
            AnalyzerSettings settings = springContext.getBean(AnalyzerSettings.class);
            try {
                springContext.registerBean(beanName, ExecutionPlanManager.class, settings);
            } catch (BeanDefinitionOverrideException ignored) {
                //
            }
        }
    }

    private static ExecutionPlanManager getExecutionPlanManager(ExtensionContext context) {
        ApplicationContext springContext = SpringExtension.getApplicationContext(context);
        return springContext.getBean(ExecutionPlanManager.class);
    }
}
