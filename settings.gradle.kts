plugins {
    `gradle-enterprise`
}

rootProject.name = "dbquerywatch"

val isCiServer = System.getenv().containsKey("CI")

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}
