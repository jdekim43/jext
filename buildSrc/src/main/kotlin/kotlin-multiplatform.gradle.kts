package convention

import org.gradle.kotlin.dsl.jvm
import org.gradle.kotlin.dsl.kotlin


plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(11)

    jvm()

//    js {
//        browser()
//        nodejs()
//    }
//
//    iosArm64()
//    iosSimulatorArm64()
}
