package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.internal.ExecutionPlanAnalyzer;
import com.parolisoft.dbquerywatch.internal.ExecutionPlanAnalyzerFactory;
import com.parolisoft.dbquerywatch.internal.ExecutionPlanManager;
import com.parolisoft.dbquerywatch.internal.QueryAnalysisExecutionListener;
import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

    private final ObjectFactory<ExecutionPlanManager> executionPlanManagerFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
            val dataSource = (DataSource) bean;
            val namedDataSource = new NamedDataSource(beanName, dataSource);
            JdbcClient jdbcClient = new SpringJdbcClient(namedDataSource);
            ExecutionPlanAnalyzer analyzer = ExecutionPlanAnalyzerFactory.create(jdbcClient);
            val listener = new QueryAnalysisExecutionListener(executionPlanManagerFactory::getObject, analyzer);
            ProxyDataSource proxyDataSource = ProxyDataSourceBuilder.create(dataSource)
                .logQueryBySlf4j(SLF4JLogLevel.INFO, ProxyDataSource.class.getName())
                .listener(listener)
                .build();
            return createAopProxy(dataSource, proxyDataSource);
        }
        return bean;
    }

    // Return an AOP proxy in order to keep the original bean type.
    // @see https://arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
    private static Object createAopProxy(Object targetObject, Object actualObject) {
        val factory = new ProxyFactory(targetObject);
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
}
