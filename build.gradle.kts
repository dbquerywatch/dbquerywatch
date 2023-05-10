@file:Suppress("SpellCheckingInspection", "HasPlatformType")

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    `java-library`
    `java-test-fixtures`

    id("io.freefair.lombok") version "8.0.1"
    id("org.springframework.boot") version "2.7.11" apply false
}

group = "com.parolisoft"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

apply(plugin = "io.spring.dependency-management")

repositories {
    mavenCentral()
}

val versions = libs.versions

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core", "jackson-databind")
    implementation("com.google.code.findbugs", "jsr305", versions.findbugs.get())
    implementation("com.jayway.jsonpath", "json-path")
    implementation("net.ttddyy", "datasource-proxy", versions.dsproxy.get())
    implementation("org.slf4j", "slf4j-api")
    implementation("org.springframework", "spring-aop")
    implementation("org.springframework", "spring-context")
    implementation("org.springframework", "spring-jdbc")

    testFixturesApi("org.junit.jupiter", "junit-jupiter-api")

    testFixturesImplementation("io.github.hakky54", "logcaptor", versions.logcaptor.get())
    testFixturesImplementation("com.fasterxml.jackson.core", "jackson-databind")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:deprecation",
        "-Xlint:unchecked",
        "-Werror",
    ))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
