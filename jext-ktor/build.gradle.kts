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
                val ktorVersion: String = kt.versions.ktor.get()

                api(project(":jext-http-server-base"))
                api(project(":jext-koin"))

                api(libs.koin.core)
                api(libs.koin.ktor)

                api(libs.cohort)
                api(libs.micrometer.prometheus)
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-host-common:$ktorVersion")
                api("io.ktor:ktor-server-auto-head-response:$ktorVersion")
                api("io.ktor:ktor-server-default-headers:$ktorVersion")
                api("io.ktor:ktor-server-double-receive:$ktorVersion")
                api("io.ktor:ktor-server-forwarded-header:$ktorVersion")
                api("io.ktor:ktor-server-status-pages:$ktorVersion")
                api("io.ktor:ktor-server-caching-headers:$ktorVersion")
                api("io.ktor:ktor-server-call-id:$ktorVersion")
                api("io.ktor:ktor-server-cors:$ktorVersion")
                api("io.ktor:ktor-server-compression:$ktorVersion")
                api("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

                compileOnly("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                compileOnly("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                compileOnly("io.ktor:ktor-serialization-gson:$ktorVersion")
                compileOnly("io.ktor:ktor-serialization-jackson:$ktorVersion")
                compileOnly("io.ktor:ktor-server-auth:$ktorVersion")

                api(jade.common.exception)
                implementation(jade.common.util)
                implementation(jade.jLogger)
                implementation(jade.jLogger.coroutine)
                implementation(jade.jLogger.ktor)
                implementation(jade.jLogger.koin)
            }
        }

        jvmMain.dependencies {
            val ktorVersion: String = kt.versions.ktor.get()

            api("io.ktor:ktor-server-netty:${ktorVersion}")
        }
    }
}
