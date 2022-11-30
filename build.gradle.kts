plugins {
    kotlin("multiplatform") version "1.7.22" apply false
    kotlin("jvm") version "1.7.22" apply false
    id("org.jetbrains.dokka") version "1.7.20" apply false
    id("maven-publish")
    id("signing")
}

allprojects {
    group = "kr.jadekim"
    version = "2.0.2"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    println(project.name)

    apply {
        plugin("org.jetbrains.dokka")
        plugin("maven-publish")
        plugin("signing")
    }

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }

    publishing {
        publications.withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set(project.name)
                description.set("Jext Libraries")
                url.set("https://github.com/jdekim43/jext")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("jdekim43")
                        name.set("Jade Kim")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/jdekim43/jext.git")
                    developerConnection.set("scm:git:git://github.com/jdekim43/jext.git")
                    url.set("https://github.com/jdekim43/jext")
                }
            }
        }

        repositories {
            val ossrhUsername: String by project
            val ossrhPassword: String by project

            if (version.toString().endsWith("-SNAPSHOT", true)) {
                maven {
                    name = "mavenCentralSnapshot"
                    setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    credentials {
                        username = ossrhUsername
                        password = ossrhPassword
                    }
                }
            } else {
                maven {
                    name = "mavenCentral"
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = ossrhUsername
                        password = ossrhPassword
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }
}
