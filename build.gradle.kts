@file:Suppress("SpellCheckingInspection", "HasPlatformType")

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    `java-library`
    `maven-publish`
    signing

    id("io.freefair.lombok") version "8.0.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.springframework.boot") version "2.7.11" apply false
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
    }
}

dependencies {
    api("org.junit.jupiter", "junit-jupiter-api")

    implementation("com.fasterxml.jackson.core", "jackson-databind")
    implementation("com.google.code.findbugs", "jsr305", versions.findbugs.get())
    implementation("com.jayway.jsonpath", "json-path")
    implementation("io.github.hakky54", "logcaptor", versions.logcaptor.get())
    implementation("net.ttddyy", "datasource-proxy", versions.dsproxy.get())
    implementation("org.slf4j", "slf4j-api")
    implementation("org.springframework", "spring-aop")
    implementation("org.springframework", "spring-context")
    implementation("org.springframework", "spring-jdbc")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "--release", "8",
        "-Xlint:deprecation",
        "-Xlint:unchecked",
        "-Werror",
    ))
}

tasks.withType<Test> {
    useJUnitPlatform()
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
