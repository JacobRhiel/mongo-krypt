plugins {
    `maven-publish`
    signing
}

repositories {
    gradlePluginPortal()
}

group = "com.mongokrypt"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":mongokrypt-shared"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4")
}



val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val ossrhUsername: String by project
val ossrhPassword: String by project

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {

        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("MongoKrypt")
            description.set("Enterprise automatic encryption library for MongoDB.")
            url.set("https://github.com/JacobRhiel/mongo-krypt")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("JacobRhiel")
                    name.set("Jacob Rhiel")
                    email.set("jacob.rhiel@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/JacobRhiel/mongo-krypt")
            }

        }
    }
}

signing {
    sign(publishing.publications)
}