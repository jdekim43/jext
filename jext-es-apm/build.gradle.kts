plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    api(libs.elastic.apm)

    implementation(kt.kotlinx.coroutine)
}
