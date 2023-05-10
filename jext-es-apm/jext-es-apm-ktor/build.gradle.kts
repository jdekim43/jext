plugins {
    kotlin("jvm")
}

dependencies {
    val jLoggerVersion: String by project

    implementation(project(":jext-es-apm"))
    compileOnly(project(":jext-ktor"))
    compileOnly("kr.jadekim:j-logger:$jLoggerVersion")
    compileOnly("kr.jadekim:j-logger-coroutine:$jLoggerVersion")
}

tasks {
    val jvmTarget: String by project

    compileKotlin {
        kotlinOptions.jvmTarget = jvmTarget
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = jvmTarget
    }
    test {
        useJUnitPlatform()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}
