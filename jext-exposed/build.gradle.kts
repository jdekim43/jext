plugins {
    id("convention.kotlin-multiplatform")
    id("convention.publish")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kt.exposed)
                api(kt.exposed.json)
                api(kt.exposed.kotlin.datetime)

                implementation(kt.kotlinx.coroutine)

                compileOnly(jade.common.util)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(kt.exposed.jdbc)
                api(kt.exposed.javatime)
                api(kt.exposed.migration.jdbc)
            }
        }
    }
}
