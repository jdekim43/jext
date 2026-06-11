plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":jext-ktor"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.sentry)
                implementation(libs.sentry.kotlin)
            }
        }
    }
}
