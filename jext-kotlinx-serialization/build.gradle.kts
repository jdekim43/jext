plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
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
                api(kt.kotlinx.serialization.json)

                compileOnly(jade.common.encoder)
            }
        }
    }
}
