plugins {
    kotlin("multiplatform")
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
                val jLoggerVersion: String by project

                api(project(":jext-http-server-base"))

                implementation("kr.jadekim:common-exception:$commonVersion")
                implementation("kr.jadekim:j-logger:$jLoggerVersion")
                implementation("kr.jadekim:j-logger-coroutine:$jLoggerVersion")
                implementation("kr.jadekim:j-logger-ktor:$jLoggerVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                val ktorVersion: String by project

                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-host-common:$ktorVersion")
                api("io.ktor:ktor-server-netty:$ktorVersion")
                api("io.ktor:ktor-gson:$ktorVersion")

                compileOnly("io.ktor:ktor-auth:$ktorVersion")
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
