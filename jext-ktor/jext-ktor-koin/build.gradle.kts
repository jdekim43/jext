plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
}

kotlin {
    targets.all {
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                    freeCompilerArgs.add("-Xcontext-receivers")
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":jext-ktor"))
                implementation(project(":jext-koin"))
            }
        }
    }
}
