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
    "jext-es-apm:jext-es-apm-exposed",
    "jext-datadog-apm",
    "jext-datadog-apm:jext-datadog-apm-ktor",
    "jext-datadog-apm:jext-datadog-apm-exposed",
    "jext-redisson"
)
