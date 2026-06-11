package convention

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

plugins {
    id("maven-publish")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml"))
    archiveClassifier.set("javadoc")
    from(tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml").flatMap { it.outputDirectory })
}

configure<PublishingExtension> {
    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set(project.name)
            description.set("Extensions for some libraries")
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
}
