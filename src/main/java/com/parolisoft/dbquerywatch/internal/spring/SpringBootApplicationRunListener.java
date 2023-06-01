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

/**
 * Used to collect some useful information from the SpringBootApplication object.
 */
class SpringBootApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private static final List<String> basePackages = new ArrayList<>();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // Retrieve the main class from the SpringApplication
    public static List<String> getBasePackages() {
        return basePackages;
    }

    // Set the main class from the SpringApplication
    private static void setBasePackages(List<String> basePackages) {
        SpringBootApplicationRunListener.basePackages.clear();
        SpringBootApplicationRunListener.basePackages.addAll(basePackages);
    }

    // Constructor for initializing the run listener with the SpringApplication
    @SuppressWarnings("unused")
    public SpringBootApplicationRunListener(SpringApplication application, String[] args) {
        setBasePackages(getBasePackages(application));
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
