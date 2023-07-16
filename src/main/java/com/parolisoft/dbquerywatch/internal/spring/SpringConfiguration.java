package com.parolisoft.dbquerywatch.internal.spring;

import com.parolisoft.dbquerywatch.internal.AnalyzerSettings;
import com.parolisoft.dbquerywatch.internal.QueryExecutionListener;
import com.parolisoft.dbquerywatch.internal.jdbc.JdbcClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


@Configuration
class SpringConfiguration {

    @Component
    @ConfigurationProperties(prefix = "dbquerywatch")
    @Getter(onMethod_ = {@Override})
    @SuppressWarnings("FieldMayBeFinal")
    static class AnalyzerProperties implements AnalyzerSettings {
        private List<String> smallTables = new ArrayList<>();
        private List<String> appBasePackages = new ArrayList<>();
    }

    @Component
    @RequiredArgsConstructor
    static class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

        private final ObjectFactory<AnalyzerSettings> analyzerSettingsFactory;

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof DataSource && !(bean instanceof ProxyDataSource)) {
                // Return an AOP proxy in order to keep the original bean type.
                // @see https://arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
                final ProxyFactory factory = new ProxyFactory(bean);
                factory.setProxyTargetClass(true);
                factory.addAdvice(new ProxyDataSourceInterceptor(analyzerSettingsFactory::getObject,
                    beanName, (DataSource) bean));
                return factory.getProxy();
            }
            return bean;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        private static class ProxyDataSourceInterceptor implements MethodInterceptor {
            private final DataSource dataSource;

            private ProxyDataSourceInterceptor(Supplier<AnalyzerSettings> analyzerSettingsSupplier, String dataSourceName, DataSource dataSource) {
                NamedDataSource namedDataSource = new NamedDataSource(dataSourceName, dataSource);
                JdbcClient jdbcClient = new SpringJdbcClient(namedDataSource);
                this.dataSource = ProxyDataSourceBuilder.create(dataSourceName + "-proxy", dataSource)
                    .listener(new QueryExecutionListener(analyzerSettingsSupplier, jdbcClient))
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
