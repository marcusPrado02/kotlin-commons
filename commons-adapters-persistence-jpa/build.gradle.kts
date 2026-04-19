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

tasks.withType<Test> {
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    environment("DOCKER_API_VERSION", "1.45")
    systemProperty("DOCKER_API_VERSION", "1.45")
    systemProperty("DOCKER_HOST", "unix:///var/run/docker.sock")
    systemProperty("api.version", "1.45")
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
