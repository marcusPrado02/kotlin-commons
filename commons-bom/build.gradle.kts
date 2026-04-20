plugins {
    `java-platform`
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("bom") {
            groupId = "com.marcusprado02"
            artifactId = "commons-bom"
            version = project.version.toString()
            from(components["javaPlatform"])
            pom {
                name.set("kotlin-commons BOM")
                description.set("Bill of Materials for kotlin-commons modules")
                url.set("https://github.com/marcusprado02/kotlin-commons")
                licenses {
                    license {
                        name.set("Apache License 2.0")
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
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/marcusprado02/kotlin-commons")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
