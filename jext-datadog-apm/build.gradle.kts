plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    api(libs.datadog.trace)

    implementation(kt.kotlinx.coroutine)
}
