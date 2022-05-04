rootProject.name = "jext"

include(
    "jext-http-server-base",
    "jext-ktor",
    "jext-koin",
    "jext-exposed",
    "jext-exposed:jext-exposed-koin",
    "jext-gson",
    "jext-kotlinx-serialization",
    "jext-es-apm",
    "jext-es-apm:jext-es-apm-ktor",
    "jext-es-apm:jext-es-apm-exposed"
)
