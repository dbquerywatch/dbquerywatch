package com.parolisoft.dbquerywatch.internal.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;

/**
 * Used to collect some useful information from the SpringBootApplication object.
 */
class SpringBootApplicationRunListener implements SpringApplicationRunListener, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // Constructor for initializing the run listener with the SpringApplication
    @SuppressWarnings("unused")
    public SpringBootApplicationRunListener(SpringApplication application, String[] args) {
        application.setDefaultProperties(singletonMap("dbquerywatch.app-base-packages", getBasePackages(application)));
    }

    private static List<String> getBasePackages(SpringApplication application) {
        return application.getAllSources().stream()
            .flatMap(source -> {
                if (source instanceof Class) {
                    Class<?> sourceClass = (Class<?>) source;
                    ComponentScan componentScan = sourceClass.getAnnotation(ComponentScan.class);
                    if (componentScan != null) {
                        return getBasePackages(componentScan, sourceClass.getPackage().getName()).stream();
                    }
                }
                return Stream.empty();
            })
            .collect(Collectors.toList());
    }

    private static List<String> getBasePackages(ComponentScan componentScan, String defaultPackageName) {
        List<String> pkgNames = new ArrayList<>();
        pkgNames.addAll(Arrays.asList(componentScan.value()));
        pkgNames.addAll(Arrays.asList(componentScan.basePackages()));
        for (Class<?> pkgClass : componentScan.basePackageClasses()) {
            pkgNames.add(pkgClass.getPackage().getName());
        }
        if (pkgNames.isEmpty()) {
            pkgNames.add(defaultPackageName);
        }
        return pkgNames;
    }
}
