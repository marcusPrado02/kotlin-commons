plugins {
    id("kotlin-commons")
}

// This module is a test-helper library with no test sources of its own.
// Kover coverage thresholds cannot be satisfied for a module with 0 tests.
kover {
    disable()
}

dependencies {
    api(platform(libs.testcontainers.bom))
    api(libs.testcontainers.core)
    api(libs.testcontainers.postgresql)
    api(libs.testcontainers.kafka)
    api(libs.testcontainers.mongodb)
    api(libs.testcontainers.mysql)
    api(libs.testcontainers.wiremock)
    api(libs.testcontainers.localstack)
}
