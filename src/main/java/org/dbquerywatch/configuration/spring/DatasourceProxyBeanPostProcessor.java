package org.dbquerywatch.configuration.spring;

import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.dbquerywatch.adapters.out.jdbc.SpringJdbcClient;
import org.dbquerywatch.application.domain.model.ImmutableNamedDataSource;
import org.dbquerywatch.application.domain.model.NamedDataSource;
import org.dbquerywatch.application.domain.service.ExecutionPlanManager;
import org.dbquerywatch.application.port.out.ExecutionPlanAnalyzer;
import org.dbquerywatch.application.port.out.JdbcClient;
import org.dbquerywatch.configuration.common.ExecutionPlanAnalyzerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;

@Component
class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

    private final ObjectFactory<ExecutionPlanManager> executionPlanManagerFactory;

    DatasourceProxyBeanPostProcessor(ObjectFactory<ExecutionPlanManager> executionPlanManagerFactory) {
        this.executionPlanManagerFactory = executionPlanManagerFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && !(bean instanceof ProxyDataSource) && !(bean instanceof AbstractRoutingDataSource)) {
            DataSource dataSource = (DataSource) bean;
            NamedDataSource namedDataSource = ImmutableNamedDataSource.of(beanName, extractProductName(dataSource), dataSource);
            JdbcClient jdbcClient = new SpringJdbcClient(namedDataSource);
            ExecutionPlanAnalyzer analyzer = ExecutionPlanAnalyzerFactory.create(jdbcClient);
            QueryAnalysisExecutionListener listener = new QueryAnalysisExecutionListener(executionPlanManagerFactory::getObject, analyzer);
            ProxyDataSource proxyDataSource = ProxyDataSourceBuilder.create(dataSource)
                .logQueryBySlf4j(SLF4JLogLevel.INFO, ProxyDataSource.class.getName())
                .listener(listener)
                .countQuery()
                .build();
            return createAopProxy(dataSource, proxyDataSource);
        }
        return bean;
    }

    // Return an AOP proxy in order to keep the original bean type.
    // @see https://arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
    private static Object createAopProxy(Object targetObject, Object actualObject) {
        ProxyFactory factory = new ProxyFactory(targetObject);
        factory.setProxyTargetClass(true);
        factory.addAdvice((MethodInterceptor) invocation -> {
            Method proxyMethod = ReflectionUtils.findMethod(actualObject.getClass(),
                invocation.getMethod().getName());
            return proxyMethod != null
                ? proxyMethod.invoke(actualObject, invocation.getArguments())
                : invocation.proceed();
        });
        return factory.getProxy();
    }

    private static String extractProductName(DataSource dataSource) {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        } catch (MetaDataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
