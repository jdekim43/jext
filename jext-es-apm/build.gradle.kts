plugins {
    kotlin("jvm")
}

dependencies {
    val esApmVersion: String by project
    val kotlinxCoroutineVersion: String by project

    api("co.elastic.apm:apm-agent-api:$esApmVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
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