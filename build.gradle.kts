plugins {
    kotlin("jvm") version "1.5.20"
}

repositories {
    mavenCentral()
}

group = "com.mongokrypt"
version = "1.0-SNAPSHOT"

subprojects {

    repositories {
        mavenCentral()
    }

    apply(plugin = "kotlin")

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

}