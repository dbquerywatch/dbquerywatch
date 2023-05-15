package com.parolisoft.dbquerywatch;

import com.parolisoft.dbquerywatch.internal.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.lang.reflect.Method;


@Configuration
class SpringConfiguration {

    @Component
    static class DatasourceProxyBeanPostProcessor implements EnvironmentAware, BeanPostProcessor {

        private Environment environment;

        @Override
        public void setEnvironment(@Nonnull Environment environment) {
            this.environment = environment;
        }

        @Override
        public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) {
            if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
                // Return an AOP proxy in order to keep the original bean type.
                // @see https://arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
                final ProxyFactory factory = new ProxyFactory(bean);
                factory.setProxyTargetClass(true);
                factory.addAdvice(new ProxyDataSourceInterceptor(environment, beanName, (DataSource) bean));
                return factory.getProxy();
            }
            return bean;
        }

        @Override
        public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) {
            return bean;
        }

        private static class ProxyDataSourceInterceptor implements MethodInterceptor {
            private final DataSource dataSource;

            public ProxyDataSourceInterceptor(Environment environment, String beanName, DataSource dataSource) {
                this.dataSource = ProxyDataSourceBuilder.create(beanName + "-proxy", dataSource)
                    .listener(new QueryExecutionListener(environment, beanName, dataSource))
                    .build();
            }

            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                final Method proxyMethod = ReflectionUtils.findMethod(this.dataSource.getClass(),
                    invocation.getMethod().getName());
                if (proxyMethod != null) {
                    return proxyMethod.invoke(this.dataSource, invocation.getArguments());
                }
                return invocation.proceed();
            }
        }
    }
}
