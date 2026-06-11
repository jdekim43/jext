plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    implementation(project(":jext-es-apm"))
    compileOnly(project(":jext-ktor"))
    compileOnly(jade.jLogger)
    compileOnly(jade.jLogger.coroutine)
}
