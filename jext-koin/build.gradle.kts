plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.koin.core)


                implementation(jade.common.util)
                implementation(jade.jLogger)
                implementation(jade.jLogger.koin)
            }
        }
    }
}
