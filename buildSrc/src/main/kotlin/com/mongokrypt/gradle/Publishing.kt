@file:Suppress("UnstableApiUsage")

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension
import org.gradle.api.plugins.JavaPluginExtension

infix fun <T> Property<T>.by(value: T) {
    set(value)
}

fun MavenPom.configureMavenCentralMetadata(project: Project) {
    name by project.name
    description by "MongoKrypt, an Enterprise MongoDB encrpytion library."
    url by "https://github.com/JacobRhiel/mongo-krypt"

    licenses {
        license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
        }
    }

    developers {
        developer {
            id by "JacobRhiel"
            name by "JacobRhiel"
            email by "jacob.rhiel@gmail.com"
        }
    }

    scm {
        url by "https://github.com/JacobRhiel/mongo-krypt"
        connection by "scm:git:git://github.com/JacobRhiel/mongo-krypt.git"
        developerConnection by "scm:git:git@github.com:JacobRhiel/mongo-krypt.git"
    }
}

fun MavenPublication.signPublicationIfKeyPresent(project: Project) {
    val keyId = System.getenv("mongokrypt.sign.key.id")
    val signingKey = System.getenv("mongokrypt.sign.key.private")
    val signingKeyPassphrase = System.getenv("mongokrypt.sign.passphrase")
    /*if (!signingKey.isNullOrBlank()) {
        project.extensions.configure<SigningExtension>("signing") {
            useInMemoryPgpKeys(keyId, signingKey.replace(" ", "\r\n"), signingKeyPassphrase)
            sign(this@signPublicationIfKeyPresent)
        }
    }*/
}

fun Project._publishing(configure: PublishingExtension.() -> Unit) {
    extensions.configure("publishing", configure)
}

fun Project._java(configure: JavaPluginExtension.() -> Unit) {
    extensions.configure("java", configure)
}