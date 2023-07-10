@file:Suppress("SpellCheckingInspection", "HasPlatformType")

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.RunConfiguration
import org.jetbrains.gradle.ext.RunConfigurationContainer

plugins {
    `java-library`
    jacoco
    `maven-publish`
    signing

    id("com.adarshr.test-logger") version "3.2.0"
    id("com.github.ksoichiro.console.reporter") version "0.6.3"
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.16"
    id("io.freefair.lombok") version "8.1.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("net.ltgt.errorprone") version "3.1.0"
    id("org.ajoberstar.grgit") version "5.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("org.openrewrite.rewrite") version "6.1.9"
}

group = "com.parolisoft"
description = "Shift-lefting the detection of slow queries"
version = grgit.describe(mapOf(
    "tags" to true,
    "always" to true,
)).removePrefix("v")
println("version: $version")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

rewrite {
    activeRecipe("org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta")
    exclusion("src/main/**")
}

repositories {
    mavenCentral()
}

val versions = libs.versions

val testBootVariant: String by project

val testBootVersion = when (testBootVariant) {
    "2" -> versions.boot2.get()
    "30", "3.0" -> versions.boot3.get()
    "31", "3.1" -> versions.boot31.get()
    else -> throw GradleException("Unknown Spring Boot variant: $testBootVariant")
}
println("Spring Boot version: $testBootVersion")

dependencies {
    rewrite("org.openrewrite.recipe:rewrite-migrate-java:2.0.6")

    modules {
        module("com.vaadin.external.google:android-json") {
            replacedBy("org.json:json", "JSON-java is now in Public Domain")
        }
    }

    api(platform("org.springframework.boot:spring-boot-dependencies:${versions.boot2.get()}"))

    api("org.junit.jupiter", "junit-jupiter-api")

    errorprone("com.google.errorprone", "error_prone_core", versions.errorprone.get())
    annotationProcessor("com.uber.nullaway", "nullaway", versions.nullaway.get())

    compileOnly("com.google.errorprone", "error_prone_annotations", versions.errorprone.get())

    implementation("com.google.code.findbugs", "jsr305", versions.findbugs.get())
    implementation("com.jayway.jsonpath", "json-path")
    implementation("net.ttddyy", "datasource-proxy", versions.dsproxy.get())
    implementation("org.json", "json", versions.orgjson.get())
    implementation("org.slf4j", "slf4j-api")
    implementation("org.springframework", "spring-aop")
    implementation("org.springframework", "spring-context")
    implementation("org.springframework", "spring-jdbc")
    implementation("org.springframework", "spring-test")
    implementation("org.springframework", "spring-web")
    implementation("org.springframework.boot", "spring-boot")
    implementation("org.springframework.boot", "spring-boot-test")

    testAnnotationProcessor("org.mapstruct", "mapstruct-processor", versions.mapstruct.get())

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$testBootVersion"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:${versions.testcontainers.get()}"))

    testImplementation("ch.qos.logback", "logback-classic")
    testImplementation("com.google.truth", "truth", versions.truth.get())
    testImplementation("com.google.truth.extensions", "truth-java8-extension", versions.truth.get())
    testImplementation("net.bytebuddy", "byte-buddy")
    testImplementation("org.jetbrains", "annotations", versions.jbannotations.get())
    testImplementation("org.junit-pioneer", "junit-pioneer", versions.junit.pioneer.get())
    testImplementation("org.junit.jupiter", "junit-jupiter-engine")
    testImplementation("org.junit.platform", "junit-platform-testkit")
    testImplementation("org.mapstruct", "mapstruct", versions.mapstruct.get())
    testImplementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot", "spring-boot-starter-data-rest")
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-webflux")
    testImplementation("org.testcontainers", "mysql")
    testImplementation("org.testcontainers", "oracle-xe")
    testImplementation("org.testcontainers", "postgresql")

    testRuntimeOnly("com.h2database", "h2")
    testRuntimeOnly("com.mysql", "mysql-connector-j")
    testRuntimeOnly("com.oracle.database.jdbc", "ojdbc11")
    testRuntimeOnly("org.flywaydb", "flyway-core")
    testRuntimeOnly("org.flywaydb", "flyway-mysql")
    testRuntimeOnly("org.postgresql", "postgresql")

    if (testBootVersion.startsWith("2.")) {
        testImplementation(platform("org.springframework.cloud:spring-cloud-sleuth-dependencies:${versions.sleuth.get()}"))
        testRuntimeOnly("org.springframework.cloud", "spring-cloud-sleuth-zipkin")
        testRuntimeOnly("org.springframework.cloud", "spring-cloud-starter-sleuth")
    } else {
        testRuntimeOnly("io.micrometer", "micrometer-tracing-bridge-brave")
        testRuntimeOnly("io.zipkin.reporter2", "zipkin-reporter-brave")
        testRuntimeOnly("org.springframework.boot", "spring-boot-starter-actuator")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:deprecation",
        "-Xlint:unchecked",
        "-Werror",
    ))
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf(
        "--release", "8",
    ))
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "com.parolisoft.dbquerywatch")
    }
}

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    options.compilerArgs.addAll(listOf(
        "-Amapstruct.suppressGeneratorTimestamp=true",
        "-Amapstruct.suppressGeneratorVersionInfoComment=true",
        "-Amapstruct.verbose=false",
    ))
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.reportCoverage)
}

testlogger {
    showSkipped = false
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            includes = listOf("*ExecutionPlanAnalyzer")
            limit {
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.reportCoverage {
    dependsOn(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

fun IdeaProject.settings(block: ProjectSettings.() -> Unit) =
    (this@settings as ExtensionAware).extensions.configure("settings", block)

fun ProjectSettings.runConfigurations(block: RunConfigurationContainer.() -> Unit) =
    (this@runConfigurations as ExtensionAware).extensions.configure("runConfigurations", block)

inline fun <reified T : RunConfiguration> RunConfigurationContainer.defaults(noinline block: T.() -> Unit) =
    defaults(T::class.java, block)

idea.project {
    settings {
        runConfigurations {
            defaults<Gradle> {
                envs = mapOf("SPRING_OUTPUT_ANSI_ENABLED" to "always")
            }
        }
    }
}

tasks.withType<Javadoc> {
    exclude("**/internal/*")
    (options as StandardJavadocDocletOptions).apply {
        noTimestamp.value = true
        docEncoding = "UTF-8"
        charSet = "UTF-8"
        encoding = "UTF-8"
        docTitle = "dbQueryWatch version ${project.version}"
        windowTitle = "dbQueryWatch ${project.version}"
        header = "<b>dbQueryWatch</b>"
        bottom = "Copyright &copy; 2023 Paroli Consulting. All Rights Reserved."
        addBooleanOption("html5", true)
        // See JDK-8200363 (https://bugs.openjdk.java.net/browse/JDK-8200363)
        // for information about the -Xwerror option.
        addBooleanOption("Xwerror", true)
    }
}

apply(from = "./gradle/dependencyUpdates.gradle.kts")

val groupId = group.toString()
val artifactId = name

val ghOrg = "parolisoft"
val ghRepo = name
val ghHostAndPath = "github.com/${ghOrg}/${ghRepo}.git"

publishing {
    publications {
        create<MavenPublication>(artifactId) {
            from(components["java"])
            version = version.toString()
            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set("https://$ghHostAndPath")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ebo")
                        name.set("Eliezio Oliveira")
                        email.set("eliezio@parolisoft.com")
                    }
                }
                scm {
                    connection.set("scm:git:$ghHostAndPath")
                    developerConnection.set("scm:git:ssh://$ghHostAndPath")
                    url.set("https://$ghHostAndPath")
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications[artifactId])
}

nexusPublishing {
    this.repositories {
        val sonatypeStagingProfileId: String? by project
        val sonatypeUsername: String? by project
        val sonatypePassword: String? by project

        if ((sonatypeStagingProfileId != null) && (sonatypeUsername != null) && (sonatypePassword != null)) {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

                stagingProfileId.set(sonatypeStagingProfileId)
                username.set(sonatypeUsername)
                password.set(sonatypePassword)
            }
        }
    }
}
