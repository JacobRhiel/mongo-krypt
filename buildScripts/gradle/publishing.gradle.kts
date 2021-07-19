apply(plugin = "java-library")
apply(plugin = "maven")
apply(plugin = "maven-publish")
apply(plugin = "signing")

_java {
    withJavadocJar()
    withSourcesJar()
}

_publishing {
    publications {
        create<MavenPublication>("MongoKryptJars") {
            artifactId = project.name
            from(project.components["java"])
            pom {
                configureMavenCentralMetadata(project)
            }
            signPublicationIfKeyPresent(project)
        }
    }
}