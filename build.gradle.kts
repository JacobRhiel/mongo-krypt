plugins {
    kotlin("jvm") version "1.5.20"
    id("io.github.gradle-nexus.publish-plugin")
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

allprojects {
    group = "com.mongokrypt"
    version = "1.0.0-SNAPSHOT"
    //apply(from = rootProject.file("buildScripts/gradle/publishing.gradle.kts"))
}

subprojects {

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    dependencies {
        api("org.slf4j:slf4j-jdk14:2.0.0-alpha1")
        api("io.github.microutils:kotlin-logging:2.0.4")
        testImplementation(platform("org.junit:junit-bom:5.8.0-M1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val ossUsername: String by project
    val ossPassword: String by project

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])
                pom {

                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "http://45.32.133.193:8081/repository/maven-releases/"
                val snapshotsRepoUrl = "http://45.32.133.193:8081/repository/maven-snapshots/"
                credentials {
                    username = ossUsername
                    password = ossPassword
                }
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                isAllowInsecureProtocol = true
            }
        }
    }

}


val ossrhUsername: String by project
val ossrhPassword: String by project
val stagingId: String by project

/*
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            stagingProfileId by stagingId
            username.set(ossrhUsername)
            password.set(ossrhPassword)
            useStaging.set(true)
        }
    }
}*/
