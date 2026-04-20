plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-messaging"))
    api(libs.kafka.clients)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
