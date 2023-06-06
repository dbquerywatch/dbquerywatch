import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gundy.semver4j.model.Version
import org.apache.commons.text.StringSubstitutor

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.github.ben-manes", "gradle-versions-plugin", "0.46.0")
        classpath("com.github.gundy", "semver4j", "0.16.4")
        classpath("org.apache.commons", "commons-text", "1.10.0")
    }
}

apply<VersionsPlugin>()

val nonStableRegex by lazy {
    "([\\.-](alpha|beta|ea|m|preview|rc)[\\.-]?\\d*|-b\\d+\\.\\d+)$".toRegex(RegexOption.IGNORE_CASE)
}

fun isNonStable(version: String): Boolean {
    return nonStableRegex.containsMatchIn(version)
}

// SemVer Ranges:
// https://devhints.io/semver#:~:text=are%20not%20matched.-,Ranges,-~1.2.3
val upgradesToIgnore = listOf(
    "ch.qos.logback:*:>\${currentVersionMajor}.\${currentVersionMinor}",
    "org.flywaydb:*:>\${currentVersionMajor}",
    "org.openrewrite.recipe:*:>\${currentVersionMajor}",
    "org.slf4j:*:>\${currentVersionMajor}",
    "org.springframework:*:>\${currentVersionMajor}",
    "org.springframework.boot:*:>\${currentVersionMajor}",
)

fun moduleMatcher(moduleSpec: String, currentVersion: String): (ModuleComponentIdentifier) -> Boolean {
    val (group, module, version) = moduleSpec.split(':')

    val currentVersionRange by lazy {
        Version.fromString(currentVersion).let { versionElements ->
            StringSubstitutor.replace(
                version,
                mapOf(
                    "currentVersionMajor" to versionElements.major,
                    "currentVersionMinor" to versionElements.minor,
                    "currentVersionPatch" to versionElements.patch,
                )
            )
        }
    }

    return {
        (it.group == group)
            && ((module == "*") || (it.module == module))
            && com.github.gundy.semver4j.SemVer.satisfies(it.version, currentVersionRange)
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) || upgradesToIgnore.any { moduleMatcher(it, currentVersion)(candidate) }
    }
}
