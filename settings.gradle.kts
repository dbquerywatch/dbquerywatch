plugins {
    id("com.gradle.develocity") version "3.17.5"
}

rootProject.name = "dbquerywatch"

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
    }
}
