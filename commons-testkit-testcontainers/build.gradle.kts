plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.testcontainers.bom))
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.testcontainers.kafka)
}
