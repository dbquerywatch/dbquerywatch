@file:Suppress("SpellCheckingInspection")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.SemVer
import com.github.gundy.semver4j.model.Version
import com.moandjiezana.toml.Toml
import io.spring.gradle.dependencymanagement.internal.bridge.InternalComponents
import org.apache.commons.text.StringSubstitutor

plugins {
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

val nonStableRegex by lazy {
    "([.-](a|alpha|b|beta|ea|m|preview|rc)[.-]?\\d*|-b\\d+\\.\\d+)$".toRegex(RegexOption.IGNORE_CASE)
}

fun isNonStable(version: String): Boolean {
    return nonStableRegex.containsMatchIn(version)
}

// SemVer Ranges:
// https://devhints.io/semver#:~:text=are%20not%20matched.-,Ranges,-~1.2.3
val upgradesToIgnore = listOf(
    "org.springframework.boot:*:>\${major}",
)

fun moduleMatcher(moduleSpec: String, currentVersion: String): (ModuleComponentIdentifier) -> Boolean {
    val (group, module, version) = moduleSpec.split(':')

    val currentVersionRange by lazy {
        Version.fromString(currentVersion).let {
            StringSubstitutor.replace(version, mapOf("major" to it.major, "minor" to it.minor, "patch" to it.patch))
        }
    }

    return {
        (it.group == group)
            && ((module == "*") || (it.module == module))
            && SemVer.satisfies(it.version, currentVersionRange)
    }
}

val versionsToml = Toml().read(file("gradle/libs.versions.toml"))
val boot2Version: String = versionsToml.getString("versions.boot2")
val dependencyManagement = InternalComponents(project).dependencyManagementExtension.apply {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$boot2Version")
    }
}

val managedVersions: Map<String, String> = dependencyManagement.managedVersions

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        val managedVersion = managedVersions["${candidate.group}:${candidate.module}"]
        (managedVersion != null && managedVersion != candidate.version)
                || isNonStable(candidate.version)
                || upgradesToIgnore.any { moduleMatcher(it, currentVersion)(candidate) }
    }
}
