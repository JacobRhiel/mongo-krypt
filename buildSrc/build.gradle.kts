repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    gradleApi()
    implementation("io.github.gradle-nexus", "publish-plugin", "1.0.0")
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin")
            useVersion("1.5.0")
        if (requested.group == "com.fasterxml.jackson.core")
            useVersion("2.12.2")
        if (requested.group == "com.fasterxml.jackson.module")
            useVersion("2.12.2")
        if (requested.group == "org.apache.httpcomponents")
            useVersion("4.4.13")
    }
}
plugins {
    `kotlin-dsl` apply true
}