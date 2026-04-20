import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.dokka")
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
