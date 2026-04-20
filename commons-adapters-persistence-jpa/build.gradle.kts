plugins {
    id("kotlin-commons-spring")
}

// Pin docker-java to 3.7.1 for Docker Engine 27+ API compatibility.
configurations.all {
    resolutionStrategy.force(
        "com.github.docker-java:docker-java-api:3.7.1",
        "com.github.docker-java:docker-java-transport:3.7.1",
        "com.github.docker-java:docker-java-transport-zerodep:3.7.1",
    )
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
    testImplementation(libs.mockk)
    runtimeOnly("org.postgresql:postgresql")
}
