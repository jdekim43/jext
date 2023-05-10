plugins {
    kotlin("jvm")
}

dependencies {
    val redissonVersion: String by project
    val kotlinxCoroutineVersion: String by project
    val gsonVersion: String by project

    api("org.redisson:redisson:$redissonVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxCoroutineVersion")

    compileOnly("com.google.code.gson:gson:$gsonVersion")
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