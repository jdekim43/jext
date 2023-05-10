plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            val jvmTarget: String by rootProject

            kotlinOptions {
                this.jvmTarget = jvmTarget
                freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":jext-ktor"))
                implementation(project(":jext-koin"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                val junitVersion: String by project

                implementation(kotlin("test-junit5"))
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
                compileOnly("org.junit.jupiter:junit-jupiter-api:$junitVersion")
                compileOnly("org.junit.jupiter:junit-jupiter-params:$junitVersion")
            }
        }
    }
}
