plugins {
    id("kotlin-commons")
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
