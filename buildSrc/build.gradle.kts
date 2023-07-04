plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
    implementation("com.github.gundy", "semver4j", "0.16.4")
    implementation("org.apache.commons", "commons-text", "1.10.0")
}
