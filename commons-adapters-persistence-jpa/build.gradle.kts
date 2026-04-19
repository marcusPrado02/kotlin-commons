plugins {
    id("kotlin-commons-spring")
}

dependencyManagement {
    dependencies {
        dependencySet("com.github.docker-java:3.7.1") {
            entry("docker-java-api")
            entry("docker-java-transport")
            entry("docker-java-transport-zerodep")
        }
    }
}

dependencies {
    api(project(":commons-ports-persistence"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    runtimeOnly("org.postgresql:postgresql")
}
