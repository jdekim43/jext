plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
    alias(kt.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(jade.common.util)
                implementation(jade.common.exception)
                implementation(jade.jLogger)

                compileOnly(kt.kotlinx.serialization.json)
            }
        }
    }
}
