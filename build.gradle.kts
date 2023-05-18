@file:Suppress("SpellCheckingInspection", "HasPlatformType")

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    `java-library`
    jacoco
    `maven-publish`
    signing

    id("com.adarshr.test-logger") version "3.2.0"
    id("com.github.ksoichiro.console.reporter") version "0.6.3"
    id("io.freefair.lombok") version "8.0.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.springframework.boot") version "2.7.12" apply false
}

apply(plugin = "io.spring.dependency-management")

group = "com.parolisoft"
description = "A test lib to help catch slow queries on integration tests"
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

repositories {
    mavenCentral()
}

val versions = libs.versions

the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom("org.testcontainers:testcontainers-bom:${versions.testcontainers.get()}")
    }
}

dependencies {
    api("org.junit.jupiter", "junit-jupiter-api")

    implementation("com.google.code.findbugs", "jsr305", versions.findbugs.get())
    implementation("com.jayway.jsonpath", "json-path")
    implementation("net.ttddyy", "datasource-proxy", versions.dsproxy.get())
    implementation("org.slf4j", "slf4j-api")
    implementation("org.springframework", "spring-aop")
    implementation("org.springframework", "spring-context")
    implementation("org.springframework", "spring-jdbc")

    runtimeOnly("com.fasterxml.jackson.core", "jackson-databind")

    testAnnotationProcessor("org.mapstruct", "mapstruct-processor", versions.mapstruct.get())

    testImplementation("ch.qos.logback", "logback-classic")
    testImplementation("org.junit.platform", "junit-platform-testkit")
    testImplementation("org.mapstruct", "mapstruct", versions.mapstruct.get())
    testImplementation("org.springframework.boot", "spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot", "spring-boot-starter-data-rest")
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.testcontainers", "mysql")
    testImplementation("org.testcontainers", "oracle-xe")
    testImplementation("org.testcontainers", "postgresql")

    testRuntimeOnly("com.h2database", "h2")
    testRuntimeOnly("com.mysql", "mysql-connector-j")
    testRuntimeOnly("com.oracle.database.jdbc", "ojdbc11")
    testRuntimeOnly("org.flywaydb", "flyway-core")
    testRuntimeOnly("org.flywaydb", "flyway-mysql")
    testRuntimeOnly("org.postgresql", "postgresql")
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
    }
}

tasks.reportCoverage {
    dependsOn(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<Javadoc> {
    exclude("**/internal/*")
}

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
