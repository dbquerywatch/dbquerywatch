@file:Suppress("SpellCheckingInspection")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.SemVer
import com.github.gundy.semver4j.model.Version
import org.apache.commons.text.StringSubstitutor

plugins {
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

val nonStableRegex by lazy {
    "([.-](alpha|beta|ea|m|preview|rc)[.-]?\\d*|-b\\d+\\.\\d+)$".toRegex(RegexOption.IGNORE_CASE)
}

fun isNonStable(version: String): Boolean {
    return nonStableRegex.containsMatchIn(version)
}

// SemVer Ranges:
// https://devhints.io/semver#:~:text=are%20not%20matched.-,Ranges,-~1.2.3
val upgradesToIgnore = listOf(
    "com.jayway.jsonpath:*:>\${major}.\${minor}",
    "org.junit.jupiter:*:>\${major}.\${minor}",
    "org.slf4j:*:>\${major}",
    "org.springframework:*:>\${major}",
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

tasks.withType<DependencyUpdatesTask> {
    filterConfigurations = Spec { !it.name.startsWith("test") }
    rejectVersionIf {
        isNonStable(candidate.version) || upgradesToIgnore.any { moduleMatcher(it, currentVersion)(candidate) }
    }
}
