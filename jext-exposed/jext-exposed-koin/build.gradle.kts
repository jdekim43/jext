plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":jext-exposed"))
                implementation(project(":jext-koin"))
                implementation(libs.koin.core)

                compileOnly(libs.hikaricp)
            }
        }
        val jvmMain by getting {
            dependencies {
                compileOnly(libs.aws.rds)
                compileOnly(libs.aws.sts)
            }
        }
    }
}
