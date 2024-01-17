plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.ben-manes", "gradle-versions-plugin", "0.50.0")
    implementation("com.github.gundy", "semver4j", "0.16.4")
    implementation("com.moandjiezana.toml", "toml4j", "0.7.2")
    implementation("io.spring.gradle", "dependency-management-plugin", "1.1.4")
    implementation("org.apache.commons", "commons-text", "1.11.0")
}
