plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.22"
}

kotlin {
    jvm {
        compilations.all {
            val jvmTarget: String by rootProject

            kotlinOptions.jvmTarget = jvmTarget
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                val commonVersion: String by project
                val kotlinxSerializationVersion: String by project
                val jLoggerVersion: String by project

                implementation("kr.jadekim:common-util:$commonVersion")
                implementation("kr.jadekim:common-exception:$commonVersion")
                implementation("kr.jadekim:j-logger:$jLoggerVersion")

                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
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
