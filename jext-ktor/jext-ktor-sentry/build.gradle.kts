plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        compilations.all {
            val jvmTarget: String by rootProject

            kotlinOptions {
                this.jvmTarget = jvmTarget
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
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                val sentryVersion: String by project

                implementation("io.sentry:sentry:$sentryVersion")
                implementation("io.sentry:sentry-kotlin-extensions:$sentryVersion")
            }
        }
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
