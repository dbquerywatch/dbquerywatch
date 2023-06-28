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
        classpath("com.github.ben-manes", "gradle-versions-plugin", "0.47.0")
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
    "com.jayway.jsonpath:*:>\${major}.\${minor}",
    "org.junit.jupiter:*:>\${major}.\${minor}",
    "org.slf4j:*:>\${major}",
    "org.springframework:*:>\${major}",
    "org.springframework.boot:*:>\${major}",
)

fun moduleMatcher(moduleSpec: String, currentVersion: String): (ModuleComponentIdentifier) -> Boolean {
    val (group, module, version) = moduleSpec.split(':')

    val currentVersionRange by lazy {
        Version.fromString(currentVersion).let { versionElements ->
            StringSubstitutor.replace(
                version,
                mapOf(
                    "major" to versionElements.major,
                    "minor" to versionElements.minor,
                    "patch" to versionElements.patch,
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
    filterConfigurations = Spec { !it.name.startsWith("test") }
    rejectVersionIf {
        isNonStable(candidate.version) || upgradesToIgnore.any { moduleMatcher(it, currentVersion)(candidate) }
    }
}
