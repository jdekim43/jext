plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    api(libs.redisson)

    implementation(kt.kotlinx.coroutine.jdk8)

    compileOnly(libs.gson)
}
