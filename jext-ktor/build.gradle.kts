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
                val ktorVersion: String by project
                val commonVersion: String by project
                val jLoggerVersion: String by project

                api(project(":jext-http-server-base"))

                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-host-common:$ktorVersion")
                api("io.ktor:ktor-server-netty:$ktorVersion")
                api("io.ktor:ktor-server-auto-head-response:$ktorVersion")
                api("io.ktor:ktor-server-default-headers:$ktorVersion")
                api("io.ktor:ktor-server-double-receive:$ktorVersion")
                api("io.ktor:ktor-server-forwarded-header:$ktorVersion")
                api("io.ktor:ktor-server-status-pages:$ktorVersion")
                api("io.ktor:ktor-server-caching-headers:$ktorVersion")
                api("io.ktor:ktor-server-call-id:$ktorVersion")
                api("io.ktor:ktor-server-cors:$ktorVersion")
                api("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-gson:$ktorVersion")

                compileOnly("io.ktor:ktor-server-auth:$ktorVersion")

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
