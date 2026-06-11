plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    implementation(project(":jext-datadog-apm"))
    compileOnly(project(":jext-ktor"))
    compileOnly(kt.ktor.client.core)
    compileOnly(jade.jLogger)
    compileOnly(jade.jLogger.coroutine)
}
