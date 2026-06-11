plugins {
    id("convention.kotlin-jvm")
    id("convention.publish")
}

dependencies {
    api(libs.gson)

    implementation(kotlin("reflect"))

    implementation(jade.common.encoder)
}