import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

detekt {
    config.setFrom(rootProject.file("detekt.yml"))
    buildUponDefaultConfig = true
}

kotlin {
    jvmToolchain(21)
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    environment("DOCKER_API_VERSION", "1.45")
    systemProperty("DOCKER_HOST", "unix:///var/run/docker.sock")
    systemProperty("DOCKER_API_VERSION", "1.45")
    systemProperty("api.version", "1.45")
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = 60
                    coverageUnits = CoverageUnit.LINE
                }
                bound {
                    minValue = 55
                    coverageUnits = CoverageUnit.BRANCH
                }
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
        ),
    )
    pom {
        name.set(project.name)
        description.set("kotlin-commons — ${project.name}")
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
