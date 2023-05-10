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
        val commonMain by getting {
            dependencies {
                val koinVersion: String by project
                val hikaricpVersion: String by project

                implementation(project(":jext-exposed"))
                implementation(project(":jext-koin"))
                implementation("io.insert-koin:koin-core:$koinVersion")

                compileOnly("com.zaxxer:HikariCP:$hikaricpVersion") {
                    exclude("org.slf4j", "slf4j-api")
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                val awsSdkVersion: String by project

                compileOnly("software.amazon.awssdk:rds:$awsSdkVersion")
                compileOnly("software.amazon.awssdk:sts:$awsSdkVersion")
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
