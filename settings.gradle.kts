rootProject.name = "jext"

include(
    "jext-http-server-base",
    "jext-ktor",
    "jext-ktor:jext-ktor-koin",
    "jext-ktor:jext-ktor-sentry",
    "jext-koin",
    "jext-exposed",
    "jext-exposed:jext-exposed-koin",
    "jext-gson",
    "jext-kotlinx-serialization",
    "jext-es-apm",
    "jext-es-apm:jext-es-apm-ktor",
    "jext-datadog-apm",
    "jext-datadog-apm:jext-datadog-apm-ktor",
    "jext-redisson"
)

dependencyResolutionManagement {

    // Use Maven Central and the Gradle Plugin Portal for resolving dependencies in the shared build logic (`buildSrc`) project.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        mavenLocal()
    }

    // Reuse the version catalog from the main build.
    versionCatalogs {
        create("kt") {
            from(files("gradle/kotlin.versions.toml"))
        }
        create("jade") {
            from(files("gradle/jade.versions.toml"))
        }
    }
}