import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-platform`
    id("com.vanniktech.maven.publish")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":commons-kernel-core"))
        api(project(":commons-kernel-errors"))
        api(project(":commons-kernel-result"))
        api(project(":commons-kernel-ddd"))
        api(project(":commons-kernel-time"))
        api(project(":commons-ports-persistence"))
        api(project(":commons-ports-messaging"))
        api(project(":commons-ports-http"))
        api(project(":commons-ports-cache"))
        api(project(":commons-ports-email"))
        api(project(":commons-testkit-testcontainers"))
        api(project(":commons-adapters-persistence-jpa"))
        api(project(":commons-adapters-cache-redis"))
        api(project(":commons-adapters-messaging-kafka"))
        api(project(":commons-adapters-http-okhttp"))
        api(project(":commons-adapters-email-smtp"))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    pom {
        name.set("kotlin-commons BOM")
        description.set("Bill of Materials for kotlin-commons modules.")
        url.set("https://github.com/marcusPrado02/kotlin-commons")
        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("marcusprado02")
                name.set("Marcus Prado Silva")
                email.set("silvamarcusprado@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/marcusPrado02/kotlin-commons")
            connection.set("scm:git:git://github.com/marcusPrado02/kotlin-commons.git")
            developerConnection.set("scm:git:ssh://github.com/marcusPrado02/kotlin-commons.git")
        }
    }
}
